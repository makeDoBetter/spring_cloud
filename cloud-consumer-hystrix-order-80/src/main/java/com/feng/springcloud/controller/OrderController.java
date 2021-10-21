package com.feng.springcloud.controller;

import com.feng.springcloud.service.FeignOrderService;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import feign.form.util.CharsetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.i18n.LocalizedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;

/**
 * 测试Hystrix客户端服务降级
 *
 * {@link DefaultProperties}注解用于指定默认全局服务降级handler
 * {@link HystrixCommand}注解不带属性将使用@DefaultProperties指定的handler
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/9/27 15:15
 * @see DefaultProperties
 * @see HystrixCommand
 * @since JDK 1.8
 */
@RestController
@DefaultProperties(defaultFallback = "getGlobalHandler")
public class OrderController {
    @Resource
    FeignOrderService service;

    /**
     * 测试请求_正常
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/consumer/payment/getOk/{id}")
    public String getOk(@PathVariable("id") int id) {
        return service.getOk(id);
    }

    /**
     * 测试除数异常
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/consumer/payment/getNum/{id}")
    @HystrixCommand
    public String getNum(@PathVariable("id") int id) {
        int i = id / 0;
        return "/consumer/payment/getNum/" + id;
    }

    /**
     * 测试除数异常
     *
     * @return String String
     */
    @GetMapping("/consumer/payment/getNullPoint")
    @HystrixCommand
    public String getNullPoint() {
        Long a = null;
        long l = a / 1;
        return "/consumer/payment/getNullPoint";
    }

    /**
     * 测试请求_超时
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/consumer/payment/getTimeout/{id}")
    @HystrixCommand(fallbackMethod = "getTimeoutHandler",
            commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")})
    public String getTimeout(@PathVariable("id") int id) {
        return service.getTimeout(id);
    }

    public String getTimeoutHandler(int id) {
        return "客户端服务异常，请稍后再试";
    }

    public String getGlobalHandler() {
        return "全部处理器GlobalHandler：客户端服务异常，请稍后再试";
    }

    @GetMapping({"/download/file3"})
    public void downloadFileByRange(HttpServletRequest request, HttpServletResponse response, String fileName, String filePath ) throws IOException {
        /*if (StringUtils.isEmpty(filePath)){
            throw new RuntimeException("参数错误！");
        }
        if(!filePath.startsWith("group")&&System.getProperty("os.name").toLowerCase().contains("win")){
            filePath = filePath.replace("/","\\");
        }*/
        System.out.println("filePath:" + filePath);
        response.setContentType("application/x-download");
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,HEAD");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.addHeader("Content-Disposition", "attachment;filename=测试0000001后端写死");
        File file = new File("C:\\Users\\fengjirong\\Desktop\\大文件下载.txt");
        long length = file.length();
        if(!request.getMethod().equalsIgnoreCase("head")){
            String[] startEnd  =  request.getHeader("Range").replaceAll("bytes=","").split("-");
            //int offset = 10;
            int offset = Integer.parseInt(startEnd[0]);
            //int end = 100;
            int end = Integer.parseInt(startEnd[1]);
            int downloadBytes = end - offset;
            response.addHeader("Content-Range","bytes "+offset+"-"+end+"/"+ length);
            OutputStream os = response.getOutputStream();
            //OutputStream os = new FileOutputStream("C:\\Users\\fengjirong\\Desktop\\大文件下载1.txt");
            byte[] fileBytes = downloadForOffset(file,offset,downloadBytes);
            os.write(fileBytes);
            os.flush();
            os.close();
        }else{
            response.addHeader("content-length", ""+ length);
            }
        }

    private byte[] downloadForOffset(File file,int offset, int downloadBytes){
        byte[] bytes = new byte[downloadBytes];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.skip(offset-1);
            int read = in.read(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }
}
