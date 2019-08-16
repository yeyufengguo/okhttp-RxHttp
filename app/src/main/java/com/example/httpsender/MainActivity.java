package com.example.httpsender;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.example.httpsender.entity.Address;
import com.example.httpsender.entity.Response;
import com.rxjava.rxlife.RxLife;

import java.io.File;
import io.reactivex.android.schedulers.AndroidSchedulers;
import rxhttp.wrapper.param.RxHttp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void bitmap(View view) {
        String imageUrl = "http://img2.shelinkme.cn/d3/photos/0/017/022/755_org.jpg@!normal_400_400?1558517697888";
        RxHttp.get(imageUrl) //Get请求
            .asBitmap()  //这里返回Observable<Response> 对象
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                ImageView ivHead = findViewById(R.id.iv_head);
                ivHead.setImageBitmap(s);
                //成功回调
            }, (OnError) error -> {
                //失败回调
                error.show("图片加载失败,请稍后再试!");
            });
    }

    //发送Get请求
    public void sendGet(View view) {
        RxHttp.get("/service/getIpInfo.php") //Get请求
            .add("ip", "63.223.108.42") //添加参数
            .addHeader("accept", "*/*") //添加请求头
            .addHeader("connection", "Keep-Alive")
            .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
            .asResponse(Address.class)  //这里返回Observable<Response> 对象
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(address -> {
                //成功回调
            }, (OnError) error -> {
                //失败回调
                error.show("发送失败,请稍后再试!");
            });
    }

    //发送Post请求
    private void sendPost() {
        RxHttp.postForm("/service/getIpInfo.php")
            .add("ip", "63.223.108.42")//添加参数
            .addHeader("accept", "*/*") //添加请求头
            .addHeader("connection", "Keep-Alive")
            .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
            .asObject(Response.class)  //这里返回Observable<Response>对象
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(response -> {
                //成功回调
            }, (OnError) error -> {
                //失败回调
                error.show("发送失败,请稍后再试!");
            });
    }

    //文件下载，不带进度
    public void download(View view) {
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .asDownload(destPath) //注意这里使用DownloadParser解析器，并传入本地路径
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //下载成功,回调文件下载路径
            }, (OnError) error -> {
                //下载失败
                error.show("下载失败,请稍后再试!");
            });
    }

    //文件下载，带进度
    public void downloadAndProgress(View view) {
        //文件存储路径
        String destPath = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".apk";
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent()//使用指定的域名
            .asDownload(destPath, progress -> {
                //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                long totalSize = progress.getTotalSize();     //要下载的总字节大小
            }, AndroidSchedulers.mainThread()) //指定 进度/成功/失败 回调线程,不指定,默认在请求所在线程回调
            .as(RxLife.as(this)) //感知生命周期
            .subscribe(s -> {//s为String类型，这里为文件存储路径
                //下载完成，处理相关逻辑
            }, (OnError) error -> {
                //下载失败，处理相关逻辑
                error.show("下载失败,请稍后再试!");
            });
    }

    //断点下载
    public void breakpointDownload(View view) {
        String destPath = getExternalCacheDir() + "/" + "Miaobo.apk";
        long length = new File(destPath).length();
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .setRangeHeader(length)  //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath) //注意这里使用DownloadParser解析器，并传入本地路径
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //下载成功,回调文件下载路径
            }, (OnError) error -> {
                //下载失败
                error.show("下载失败,请稍后再试!");
            });
    }

    //断点下载，带进度
    public void breakpointDownloadAndProgress(View view) {
        String destPath = getExternalCacheDir() + "/" + "Miaobo.apk";
        long length = new File(destPath).length();
        RxHttp.get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent()//使用指定的域名
            .setRangeHeader(length)  //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath, length, progress -> { //如果需要衔接上次的下载进度，则需要传入上次已下载的字节数length
                //下载进度回调,0-100，仅在进度有更新时才会回调
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已下载的字节大小
                long totalSize = progress.getTotalSize();     //要下载的总字节大小
            }, AndroidSchedulers.mainThread()) //指定 进度/成功/失败 回调线程,不指定,默认在请求所在线程回调
            .as(RxLife.as(this)) //加入感知生命周期的观察者
            .subscribe(s -> { //s为String类型
                //下载成功，处理相关逻辑
            }, (OnError) error -> {
                //下载失败，处理相关逻辑
                error.show("下载失败,请稍后再试!");
            });
    }


    //文件上传，不带进度
    private void upload() {
        RxHttp.postForm("http://...") //发送Form表单形式的Post请求
            .add("file1", new File("xxx/1.png"))
            .add("file2", new File("xxx/2.png"))
            .asString() //from操作符，是异步操作
            .as(RxLife.asOnMain(this))  //感知生命周期，并在主线程回调
            .subscribe(s -> {
                //成功回调
            }, (OnError) error -> {
                //失败回调
                error.show("上传失败,请稍后再试!");
            });
    }

    //上传文件，带进度
    private void uploadAndProgress() {
        String url = "http://www.......";
        RxHttp.postForm(url) //发送Form表单形式的Post请求
            .add("file1", new File("xxx/1.png"))
            .add("file2", new File("xxx/2.png"))
            .add("key1", "value1")//添加参数，非必须
            .add("key2", "value2")//添加参数，非必须
            .addHeader("versionCode", "100")//添加请求头,非必须
            .asUpload(progress -> {
                //上传进度回调,0-100，仅在进度有更新时才会回调
                int currentProgress = progress.getProgress(); //当前进度 0-100
                long currentSize = progress.getCurrentSize(); //当前已上传的字节大小
                long totalSize = progress.getTotalSize();     //要上传的总字节大小
            }, AndroidSchedulers.mainThread()) //主线程回调,不指定,默认在请求执行线程回调
            .as(RxLife.as(this)) //加入感知生命周期的观察者
            .subscribe(s -> { //s为String类型，由SimpleParser类里面的泛型决定的
                //上传成功，处理相关逻辑
            }, (OnError) error -> {
                //上传失败，处理相关逻辑
                error.show("上传失败,请稍后再试!");
            });
    }

    //多任务下载
    public void multitaskDownload(View view) {
        startActivity(new Intent(this, DownloadMultiActivity.class));
    }
}
