package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Category2VO;
import org.redisson.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {
    @Resource
    CategoryService categoryService;
    @Resource
    RedissonClient redisson;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/", "/index.html"})
    public String getIndexPage(Model model) {
        List<CategoryEntity> list = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", list);
        return "index";
    }

    @GetMapping("index/catalog.json")
    @ResponseBody
    public Map<String, List<Category2VO>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }

    @GetMapping("hello")
    @ResponseBody
    public String hello() {
        RLock lock = redisson.getLock("my-lock");
        lock.lock();
        try {
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }


    @GetMapping("read")
    @ResponseBody
    public String read() {
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("read-write");
        RLock rLock = readWriteLock.readLock();
        String s = "";
        try {
            rLock.lock();
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
            s = stringRedisTemplate.opsForValue().get("writeValue");
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("释放锁..." + Thread.currentThread().getId());
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("write")
    @ResponseBody
    public String write() {
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("read-write");
        RLock rLock = readWriteLock.writeLock();
        String s = "";
        try {
            rLock.lock();
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set("writeValue", s);
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("释放锁..." + Thread.currentThread().getId());
            rLock.unlock();
        }
        return s;
    }


    @GetMapping(value = "/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        //获取一个信号、获取一个值,占一个车位
        boolean flag = park.tryAcquire();
        if (flag) {
            //执行业务
        } else {
            return "error";
        }
        return "ok=>" + flag;
    }

    @GetMapping(value = "/go")
    @ResponseBody
    public String go() {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();     //释放一个车位
        return "ok";
    }


    @GetMapping(value = "/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();       //等待闭锁完成
        return "放假了...";
    }

    @GetMapping(value = "/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();       //计数-1
        return id + "班的人都走了...";
    }
}
