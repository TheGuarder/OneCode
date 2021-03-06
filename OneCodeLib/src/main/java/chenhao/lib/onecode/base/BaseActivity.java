package chenhao.lib.onecode.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.Unbinder;
import chenhao.lib.onecode.OneCode;
import chenhao.lib.onecode.R;

import org.simple.eventbus.EventBus;

import butterknife.ButterKnife;
import chenhao.lib.onecode.net.HttpClient;
import chenhao.lib.onecode.utils.ActivityTask;
import chenhao.lib.onecode.utils.BarUtils;
import chenhao.lib.onecode.utils.StringUtils;
import chenhao.lib.onecode.utils.UiUtil;
import chenhao.lib.onecode.view.AlertMsg;

public abstract class BaseActivity extends FragmentActivity {
    
    protected float dp;
    protected int screenW, screenH;
    protected Unbinder unbinder;
    
    public abstract String getPageName();
    
    protected abstract void systemStatusAction(int status);
    
    protected int getStatusBarColor() {
        return -1;
    }
    
    protected boolean statusBarIsLightMode() {
        return false;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dp = getResources().getDisplayMetrics().density;
        screenW = getResources().getDisplayMetrics().widthPixels;
        screenH = getResources().getDisplayMetrics().heightPixels;
        super.onCreate(savedInstanceState);
        EventBus.getDefault().registerSticky(this);
        UiUtil.init().closeBroads(this);
        ActivityTask.init().addTask(this);
        if (getStatusBarColor() != -1) {
            BarUtils.setStatusBarColor(this, getStatusBarColor());
        } else if (null != OneCode.getConfig() && OneCode.getConfig().getDefaultStatusBarColor(this) != -1) {
            UiUtil.init().setWindowStatusBarColor(this, OneCode.getConfig().getDefaultStatusBarColor(this));
        }
        BarUtils.setStatusBarLightMode(this, statusBarIsLightMode());
        if (!OneCode.projectOK()) {
            new AlertMsg(this, new AlertMsg.OnAlertMsgListener() {
                @Override
                public boolean onClick(boolean isLeft) {
                    ActivityTask.init().clearTask();
                    System.exit(0);
                    return true;
                }
            }).setMsg(OneCode.projectErrorHint(), "知道了", "").setCancelable(false).createShow();
        }
    }
    
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (null == unbinder) {
            unbinder = ButterKnife.bind(this);
        }
    }
    
    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        if (null == unbinder) {
            unbinder = ButterKnife.bind(this);
        }
    }
    
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        if (null == unbinder) {
            unbinder = ButterKnife.bind(this);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (null != OneCode.getConfig()) {
            OneCode.getConfig().onActivityPause(this, getPageName());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (null != OneCode.getConfig()) {
            OneCode.getConfig().onActivityResume(this, getPageName());
        }
    }
    
    public static final int SYSTEM_STATUS_HIDE = 0;
    public static final int SYSTEM_STATUS_LOADING = 1;
    public static final int SYSTEM_STATUS_NULL_DATA = 2;
    public static final int SYSTEM_STATUS_NET_ERROR = 3;
    public static final int SYSTEM_STATUS_API_ERROR = 4;
    private LinearLayout system_status_layout;
    
    public LinearLayout getSystemStatusLayout() {
        if (null == system_status_layout && null != findViewById(R.id.system_status)) {
            system_status_layout = (LinearLayout) findViewById(R.id.system_status);
            system_status_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
        return system_status_layout;
    }
    
    public void showSystemStatus(int status) {
        if (null != getSystemStatusLayout()) {
            if (getSystemStatusLayout().getChildCount() > 0) {
                getSystemStatusLayout().removeAllViews();
            }
            if (status == SYSTEM_STATUS_HIDE) {
                setVis(getSystemStatusLayout(), View.GONE);
            } else {
                View statusView = null != OneCode.getConfig() ? OneCode.getConfig().getSystemStatusView(this, getPageName(), status) : null;
                if (null == statusView && status == SYSTEM_STATUS_LOADING) {
                    statusView = View.inflate(this, R.layout.onecode_layout_loading_small, null);
                } else if (null == statusView) {
                    ImageView system_status_icon = new ImageView(getSystemStatusLayout().getContext());
                    int iconResId = status == SYSTEM_STATUS_NET_ERROR || status == SYSTEM_STATUS_API_ERROR ?
                        R.drawable.onecode_default_icon_internet : R.drawable.onecode_default_icon_nothing;
                    system_status_icon.setImageResource(iconResId);
                    system_status_icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    statusView = system_status_icon;
                }
                if (null != statusView) {
                    if (status != SYSTEM_STATUS_LOADING) {
                        statusView.setTag(status);
                        statusView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int s = SYSTEM_STATUS_NULL_DATA;
                                try {
                                    if (StringUtils.isNotEmpty(v.getTag())) {
                                        s = Integer.parseInt(v.getTag().toString());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                systemStatusAction(s);
                            }
                        });
                    }
                    getSystemStatusLayout().addView(statusView);
                }
                setVis(getSystemStatusLayout(), View.VISIBLE);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        ActivityTask.init().checkRemove(this);
        UiUtil.init().closeBroads(this);
        HttpClient.init().cancel(this);
        UiUtil.init().cancelDialog();
        if (null != unbinder) {
            unbinder.unbind();
        }
        super.onDestroy();
    }
    
    public <T extends View> T findV(int id) {
        if (null != findViewById(id)) {
            return (T) findViewById(id);
        } else {
            return null;
        }
    }
    
    public <T extends View> T findV(View root, int id) {
        if (null != root && null != root.findViewById(id)) {
            return (T) root.findViewById(id);
        } else {
            return null;
        }
    }
    
    public void setVis(View v, int vis) {
        if (null != v && v.getVisibility() != vis) {
            v.setVisibility(vis);
        }
    }
    
    public void setVis(View v, boolean vis) {
        setVis(v, vis ? View.VISIBLE : View.GONE);
    }
    
}
