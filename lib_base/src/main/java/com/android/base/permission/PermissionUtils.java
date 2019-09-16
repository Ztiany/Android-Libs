package com.android.base.permission;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.android.base.R;
import com.blankj.utilcode.util.AppUtils;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2019-08-22 14:17
 */
public class PermissionUtils {

    public static String createPermissionText(Context context, @NonNull List<String> perms) {
        List<String> permList = Permission.transformText(context, perms);
        String characterSeg = context.getString(R.string.Base_character_seg);
        StringBuilder sb = new StringBuilder();
        int size = permList.size();
        for (int i = 0; i < size; i++) {
            sb.append(permList.get(i));
            if (i < size - 1) {
                sb.append(characterSeg);
            }
        }
        return sb.toString();
    }

    static CharSequence createPermissionRationaleText(Context context, @NonNull String[] perms, @ColorInt int color) {
        String permissionText = createPermissionText(context, Arrays.asList(perms));
        return tintText(context.getString(R.string.Base_request_permission_rationale, permissionText), permissionText, color);
    }

    static CharSequence createPermissionAskAgainText(Context context, @NonNull String[] permission, @ColorInt int color) {
        String permissionText = createPermissionText(context, Arrays.asList(permission));
        String appName = AppUtils.getAppName();
        String content = context.getString(R.string.Base_permission_denied_ask_again_rationale, appName, permissionText);
        return tintText(content, permissionText, color);
    }

    static CharSequence createPermissionDeniedTip(Context context, String[] permission, @ColorInt int color) {
        String permissionText = createPermissionText(context, Arrays.asList(permission));
        return tintText(context.getString(R.string.Base_permission_denied, permissionText), permissionText, color);
    }

    private static CharSequence tintText(String content, String perms, @ColorInt int color) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(content);
        int indexPerm = content.indexOf(perms);
        ssb.setSpan(new ForegroundColorSpan(color), indexPerm, indexPerm + perms.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

}
