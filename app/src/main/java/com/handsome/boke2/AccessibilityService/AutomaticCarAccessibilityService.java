package com.handsome.boke2.AccessibilityService;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutomaticCarAccessibilityService extends AccessibilityService {

    private List<AccessibilityNodeInfo> parents;
    private MediaPlayer mMediaPlayer;
    //区域
    public static String tv_cargetOregion;
    //站点
    public static String tv_cargetStation;
    public static String tv_backstation;

    //默认里程
    public static int mileage = 45;

    //已经更换车辆
    private boolean choosed = false;

    //是否指定了取车地点
    private boolean hasSpecify = false;

    /**
     * 当启动服务的时候就会被调用
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        parents = new ArrayList<>();
    }

    /**
     * 监听窗口变化的回调
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
//            //当通知栏发生改变时
//            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//                List<CharSequence> texts = event.getText();
//                if (!texts.isEmpty()) {
//                    for (CharSequence text : texts) {
//                        String content = text.toString();
//                        if (content.contains("[微信红包]")) {
//                            //模拟打开通知栏消息，即打开微信
//                            if (event.getParcelableData() != null &&
//                                    event.getParcelableData() instanceof Notification) {
//                                Notification notification = (Notification) event.getParcelableData();
//                                PendingIntent pendingIntent = notification.contentIntent;
//                                try {
//                                    pendingIntent.send();
//                                    Log.e("demo", "进入微信");
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//                }
//                break;
            //当窗口的状态发生改变时
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                Log.e("tgl===", "cl: " + className);
                if (className.equals("com.woasis.smp.activity.Main_Activity_V1")) {
                    //地图页面
                    choosed = false;
                    SystemClock.sleep(200);
                    inputClickForText("一键用车");
                } else if (className.equals("com.woasis.smp.activity.OrderSelecterActivity")) {
                    //订单页面
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();

                    if (!hasSpecify && !TextUtils.isEmpty(tv_cargetOregion) &&
                            !TextUtils.isEmpty(tv_cargetStation)) {
                        inputClickForId("com.woasis.smp:id/tv_cargetStation");
                        return;
                    }
                    boolean oderBuy = false;//是点击返回还是订车
                    boolean replace = false;//是否换车

                    //里程
                    for (int i = 0; i < 7; i++) {
                        SystemClock.sleep(200);
                        String tv_mileage = getNoteInfoText(rootNode, "com.woasis.smp:id/tv_mileage");
                        if (!TextUtils.isEmpty(tv_mileage)) {
                            int tv_number = Integer.parseInt(getNumber(tv_mileage));
                            if (tv_number < mileage) {//小于目标里程，设置换车
                                replace = true;
                            }
                            String carNum = getNoteInfoText(rootNode,"com.woasis.smp:id/tv_license");
                            if (carNum.contains("测试")){
                                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                                return;
                            }
                            oderBuy = true;
                            i = 10;
                        }
                    }

                    if (!oderBuy) {
                        if (!TextUtils.isEmpty(tv_cargetOregion) &&
                                !TextUtils.isEmpty(tv_cargetStation)) {
                            inputClickForId("com.woasis.smp:id/tv_cargetStation");
                            return;
                        }
                    }

                    if (!choosed && replace) {
                        List<AccessibilityNodeInfo> lists = rootNode.findAccessibilityNodeInfosByViewId("com.woasis.smp:id/btn_change");
                        if (lists != null && lists.size() != 0) {
                            AccessibilityNodeInfo clickChange = lists.get(0);
                            if (clickChange.isVisibleToUser()) {
                                clickChange.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                return;
                            }
                        }
                    }
                    if (oderBuy) {//订车
//                        Log.e("tgl===", "订车");
                        inputClickForId("com.woasis.smp:id/postorder");
                        startAlarm();
                    } else {//返回继续监控
//                        inputClick("com.woasis.smp:id/im_back");
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    }
                } else if (className.equals("com.woasis.smp.activity.ChangeVehicleActivity")) {
                    //选择车辆
                    clickListView(getRootInActiveWindow(), "com.woasis.smp:id/lv_change_vehicle");
                } else if (className.equals("com.woasis.smp.view.i")) {
                    //选择车辆页面-确认更换车辆
                    List<AccessibilityNodeInfo> sureList = getRootInActiveWindow()
                            .findAccessibilityNodeInfosByViewId("com.woasis.smp:id/tv_true");
                    if (sureList != null && sureList.size() > 0) {
                        sureList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        choosed = true;
                    }
                } else if (className.equals("com.woasis.smp.activity.ChoiseStationActivity")) {
                    //选择网点
                    SystemClock.sleep(200);
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    boolean hasCity = false;
                    for (int i = 0; i < 7; i++) {//查看区域
                        List<AccessibilityNodeInfo> cityList = rootNode.findAccessibilityNodeInfosByViewId("com.woasis.smp:id/listcity");
                        if (cityList != null && cityList.size() != 0) {
                            AccessibilityNodeInfo cityNode = cityList.get(0);
                            if (cityNode.getChildCount() > 0) {
                                i = 10;
                                hasCity = true;
                            } else {
                                SystemClock.sleep(200);
                            }
                        } else {
                            SystemClock.sleep(200);
                        }
                    }
                    if (hasCity) {
                        inputClickForText(true, tv_cargetOregion);
                    } else {//返回
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                        return;
                    }
                    SystemClock.sleep(200);
                    boolean hasTown = false;//乡镇
                    List<AccessibilityNodeInfo> townList = rootNode.findAccessibilityNodeInfosByViewId("com.woasis.smp:id/liststaticion");
                    AccessibilityNodeInfo townNode = townList.get(0);
                    for (int i = 0; i < 7; i++) {//查看乡镇
                        if (townList != null && townList.size() != 0) {
                            if (townNode.getChildCount() > 0) {
                                i = 10;
                                hasTown = true;
                            } else {
                                SystemClock.sleep(200);
                            }
                        } else {
                            SystemClock.sleep(200);
                        }
                    }
                    if (hasTown) {
                        inputClickTownView(townNode,tv_cargetStation);
                        hasSpecify = true;
                    } else {
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                        return;
                    }
                }
                break;
        }
    }

    /**
     * 通过ID获取控件，并进行模拟点击
     *
     * @param clickId
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void inputClickForId(String clickId) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(clickId);
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    private void inputClickForText(String chickText) {
        inputClickForText(false, chickText);
    }

    /**
     * 按文字查找按钮并点击
     *
     * @param clickParent 是否点击文字按钮父view
     * @param clickText
     */
    private void inputClickForText(boolean clickParent, String clickText) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(clickText);
            for (AccessibilityNodeInfo item : list) {
                if (clickParent) {
                    item.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    /**
     * 按文字查找 取车点并点击
     *
     * @param clickText
     */
    private void inputClickTownView(AccessibilityNodeInfo nodeInfo, String clickText) {
        for (int i = 0; i < 5; i++) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(clickText);
            if (list==null||list.size()==0){
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }else {
                for (AccessibilityNodeInfo item : list) {//按道理只有唯一一个view
                    if (item.getParent().isClickable()) {
                        item.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return;
                    } else {

                        //滑动listView
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    }
                }
            }
            SystemClock.sleep(80);
        }
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /**
     * 获取按键进行点击
     */
    private void getLastPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        recycle(rootNode);
        if (parents.size() > 0) {
            parents.get(parents.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 回归函数遍历每一个节点，并将含有"领取红包"存进List中
     *
     * @param info
     */
    public void recycle(AccessibilityNodeInfo info) {
        if (info == null) {
            return;
        }
        if (info.getChildCount() == 0) {
            if (info.getText() != null) {
                Log.e("tgl===", "text: " + info.getText().toString());
                if ("一键用车".equals(info.getText().toString())) {
                    if (info.isClickable()) {
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parents.add(parent);
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    /**
     * 中断服务的回调
     */
    @Override
    public void onInterrupt() {

    }


    /**
     * 车辆预定
     *
     * @param info
     * @return
     */
    private boolean orderCar(AccessibilityNodeInfo info) {
        if (info == null) {
            return false;
        }
        boolean title = false;
        boolean changeCare = false;
        for (int i = 0; i < info.getChildCount(); i++) {
            if (info.getChild(i) != null) {
                if (info.getText() != null) {
                    if ("车辆预定".equals(info.getText().toString())) {
                        if (!info.isClickable()) {
                            title = true;
                            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    } else if ("点击换车".equals(info.getText().toString())) {
                        if (info.isClickable()) {
                            changeCare = true;
                        }
                    }
                }
            }
        }
        //可以点击一键用车
        if (title && changeCare) {
            return true;
        }
        return false;
    }

    /**
     * 获取按钮文字; null:表示按钮不存在；空字符串：处于隐藏状态
     *
     * @param rootNode
     * @param scoureId
     * @return
     */
    private String getNoteInfoText(AccessibilityNodeInfo rootNode, String scoureId) {
        List<AccessibilityNodeInfo> lists = rootNode.findAccessibilityNodeInfosByViewId(scoureId);
        if (lists != null && lists.size() != 0) {
            AccessibilityNodeInfo info = lists.get(0);
            if (info != null) {
                if (info.isVisibleToUser()) {
                    return lists.get(0).getText().toString();
                }
                return "";
            }
            return null;

        }
        return null;
    }

    /**
     * 选择车辆
     *
     * @param rootNode
     * @param scoureId
     */
    private void clickListView(AccessibilityNodeInfo rootNode, String scoureId) {
        int maxIndex = 0;
        int last_mileage = 0;
        List<AccessibilityNodeInfo> lists = rootNode.findAccessibilityNodeInfosByViewId(scoureId);
        if (lists != null && lists.size() != 0) {
            AccessibilityNodeInfo info = lists.get(0);
            for (int i = 0; i < info.getChildCount(); i++) {
                AccessibilityNodeInfo child = info.getChild(i);
                if (child != null) {
                    List<AccessibilityNodeInfo> mChilds = child.findAccessibilityNodeInfosByViewId("com.woasis.smp:id/tv_item_mileage");
                    if (mChilds != null && mChilds.size() > 0) {
                        String item_mileage = mChilds.get(0).getText().toString();
                        int curr_mileage = Integer.parseInt(getNumber(item_mileage));
                        if (curr_mileage > last_mileage) {
                            maxIndex = i;
                            last_mileage = curr_mileage;
                        }
                    }
                }
            }
            info.getChild(maxIndex).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 选择区域，因为listView子View不可见时不能点击
     *
     * @param rootNode
     */
    private void clickListTownView(AccessibilityNodeInfo rootNode, String townText) {
        int target = 0;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            List<AccessibilityNodeInfo> townInfo = nodeInfo.findAccessibilityNodeInfosByText(townText);
            if (townInfo == null || townInfo.size() == 0) {
                rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            } else {
                target = i;
                i = Integer.MAX_VALUE;
            }
        }
//            AccessibilityNodeInfo info = lists.get(0);
//            rootNode.
//            info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            for (int i = 0; i < info.getChildCount(); i++) {
//                AccessibilityNodeInfo child = info.getChild(i);
//                if (child != null) {
//                    List<AccessibilityNodeInfo> mChilds = child.findAccessibilityNodeInfosByViewId("com.woasis.smp:id/tv_item_mileage");
//                    if (mChilds != null && mChilds.size() > 0) {
//                        String item_mileage = mChilds.get(0).getText().toString();
//                        int curr_mileage = Integer.parseInt(getNumber(item_mileage));
//                        if (curr_mileage > last_mileage) {
//                            maxIndex = i;
//                            last_mileage = curr_mileage;
//                        }
//                    }
//                }
//            }
//            info.getChild(maxIndex).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//        }
    }

    public String getNumber(String msg) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(msg);
        return m.replaceAll("").trim();
    }

    /**
     * 播放铃声
     */
    private void startAlarm() {
        mMediaPlayer = MediaPlayer.create(this, getSystemDefaultRingtoneUri());
        mMediaPlayer.setLooping(false);
        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }

    //获取系统默认铃声的Uri
    private Uri getSystemDefaultRingtoneUri() {
        return RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
        }
        super.onDestroy();

    }
}
