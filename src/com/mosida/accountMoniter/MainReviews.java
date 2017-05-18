package com.mosida.accountMoniter;

import com.mosida.accountMoniter.Utils.FileUtils;
import com.mosida.accountMoniter.Utils.PhoneUtils;
import com.mosida.accountMoniter.account.Comment;
import com.mosida.accountMoniter.account.GmailAccounts;
import com.mosida.accountMoniter.account.GmailInfo;
import com.mosida.accountMoniter.account.LoginByWeb;
import com.mosida.accountMoniter.vpn.IpUtils;
import com.mosida.accountMoniter.vpn.VpnParseUtils;
import com.mosida.geny.GenyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mosida on 5/14/17.
 */
public class MainReviews {

    //    public static String reviewFile = "com.blhd.tw";
//    public static String appCreator = "springgame";
    public static String reviewFile = "com.movga.force.na.gp";
    public static String appCreator = "Movga Games";
//    public static String vpnCountry = "US";
    // device
    public static String originDevice = "GoogleNexus4-en-dev";
    public static String targetDevice = "myOffer";

    public static int num = 10;
    public static GmailAccounts gmailAccounts;
    public static String accountSource = "accountcool";
    public static String tempDirectory = "/home/mosida/Documents/reviews-projects/Temp/";
    public static String tempFile = "myaccount";
    public static String targetFile = "sdcard/myaccount";
    public static String lastCommentFile = "lastComment";


    public static String recordsFile = "records";
    public static Logger logger = LogManager.getLogger(MainReviews.class);
    public static List<GmailInfo> gmails;
    public static List<String> country;


    public static void main(String[] args) throws Exception {
        // log 配置
        String info = "GP-ReviewsMission";
        System.setProperty("log4j2.saveFile",info);
        org.apache.logging.log4j.core.Logger rootLogger = (org.apache.logging.log4j.core.Logger) LogManager
                .getRootLogger();
        LoggerContext context = rootLogger.getContext();
        context.reconfigure();

        // 加载帐号信息
        gmailAccounts = new GmailAccounts();
        gmails = gmailAccounts.loadAccounts(accountSource);
        if (gmails==null){
            logger.info("获取帐号时地址错误");
            return;
        }else if(gmails.size()<=0){
            logger.info("获取帐号时为空 "+ gmails.size());
            return;
        }
        // 加载评论信息
        List<String> comments = Comment.loadComments(reviewFile);
        if (gmails==null){
            logger.info("评论为 null");
        }else if(gmails.size()<=0){
            logger.info("评论文件没有内容 : "+ gmails.size());
        }
        // 加载上次评论的序号
        int lastComment = Comment.loadLastComment(lastCommentFile);

        // country
        country = new ArrayList<>();
//        country.add("nlam");
//        country.add("tw3");


        for (int i=0; i<num; i++){
            // 更换ip
//            country = VpnParseUtils.getVpnSupportCountry(vpnCountry);
//            IpUtils.changeVpn(country);
            // 登录帐号
            if (LoginByWeb.loginByChrome(gmails.get(i))){
                logger.info("帐号登录成功");
            }else{
                logger.info("帐号登录失败");
                return;
            }
            // 获取评论
            if (lastComment == 0 || lastComment >= comments.size()){
                lastComment = 0;
            }
            logger.info("lastComment is : "+ lastComment);
            lastComment++;

            // 创建文件
            String accountData = gmails.get(i).email + ","+gmails.get(i).password+","+gmails.get(i).gid+","+comments.get(lastComment)+","+reviewFile+","+appCreator;
            FileUtils.writeFile(tempDirectory+tempFile, accountData, false);
            // copy geny 模拟器
            GenyUtils.deleteGeny(targetDevice);
            GenyUtils.cloneGeny(originDevice, targetDevice);
            // 打开模拟器
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    GenyUtils.startGeny(targetDevice);
                }
            });
            t.start();
            Thread.sleep(15000);
            // 做评论任务
            GenyUtils.pushFile(tempDirectory+tempFile, targetFile);
            PhoneUtils.startMissionService();
            Thread.sleep(40000);
            // CP 内容到本地
            /**
             * 暂时没写好
             */
            // 停止模拟器
            GenyUtils.stopGeny(targetDevice);
            // 填写评论
            Comment.writeLastComment(lastCommentFile, String.valueOf(lastComment));
            // 记录到本地
            StringBuffer recordSB = new StringBuffer();
            recordSB.append(accountData);
            recordSB.append(",");
            recordSB.append(getTimeString());
            GmailAccounts.writeReviewByAccount(reviewFile, recordSB.toString());
        }

//        IpUtils.exitVpn();

    }

    public static String getTimeString(){
        Date date=new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time=format.format(date);
        return time;
    }
}