package jk.cordova.plugin.kiosk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import org.apache.cordova.*;
import android.widget.*;
import android.view.Window;
import android.view.View;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import jk.cordova.plugin.kiosk.KioskActivity;
import org.json.JSONObject;
import java.lang.Integer;
import java.util.HashSet;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.R;
import android.view.LayoutInflater;
import android.graphics.PixelFormat;
import android.view.Gravity;

public class KioskPlugin extends CordovaPlugin {
    
    public static final String EXIT_KIOSK = "exitKiosk";
    public static final String IS_IN_KIOSK = "isInKiosk";
    public static final String IS_SET_AS_LAUNCHER = "isSetAsLauncher";
    public static final String SET_ALLOWED_KEYS = "setAllowedKeys";
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (IS_IN_KIOSK.equals(action)) {
                
                callbackContext.success(Boolean.toString(KioskActivity.running));
                return true;
                
            } else if (IS_SET_AS_LAUNCHER.equals(action)) {
                
                String myPackage = cordova.getActivity().getApplicationContext().getPackageName();
                callbackContext.success(Boolean.toString(myPackage.equals(findLauncherPackageName())));
                return true;
                
            } else if (EXIT_KIOSK.equals(action)) {
                KioskActivity.isExitingOnPause = true;
                KioskActivity.isExitingFocus = true;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                Intent chooser = Intent.createChooser(intent, "Select destination...");
                if (intent.resolveActivity(cordova.getActivity().getPackageManager()) != null) {
                    cordova.getActivity().startActivity(chooser);
                }
                
                callbackContext.success();
                return true;
                
            } else if (SET_ALLOWED_KEYS.equals(action)) {
                
                System.out.println("setAllowedKeys: " + args.toString());
                HashSet<Integer> allowedKeys = new HashSet<Integer>();
                for (int i = 0; i < args.length(); i++) {
                    allowedKeys.add(args.optInt(i));
                }
                KioskActivity.allowedKeys = allowedKeys;
                
                callbackContext.success();
                return true;
            }
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        }
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        hideSystemUI();

        // disable status bar swipe
        RelativeLayout mMainLayout = (RelativeLayout) LayoutInflater.from(this).inflate(cordova.getActivity().getResources("activity_fullscreen", "layout", cordova.getActivity().getPackageName()), null);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        WindowManager.LayoutParams handleParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        handleParams.gravity = Gravity.TOP;

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(mMainLayout, handleParams);
    }
    
    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        hideSystemUI();
    }
    
    private String findLauncherPackageName() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = this.cordova.getActivity().getPackageManager().resolveActivity(intent, 0);
        return res.activityInfo.packageName;
    }

    private void hideSystemUI() {
        View mDecorView = cordova.getActivity().getWindow().getDecorView();
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mDecorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                cordova.getActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        });
    }
}

