//package com.erank.radiokoletsionv2.account_managers;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.erank.radiokoletsionv2.fragments.ProfileFragment;
//import com.erank.radiokoletsionv2.activities.MainActivity;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.GraphRequest;
//import com.facebook.login.LoginManager;
//import com.facebook.login.LoginResult;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import static android.content.Context.MODE_PRIVATE;
//
//public class FacebookAccountManager {
//    public static FacebookCallback<LoginResult> getFacebookCallBack(Activity activity) {
//        return new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                GraphRequest request = GraphRequest.newMeRequest(
//                        loginResult.getAccessToken(),
//                        (object, response) -> {
//                            Log.v("Main", response.toString());
//                            FacebookAccountManager.writeProfileToPrefs(object,
//                                    activity);
//                        });
//                Bundle parameters = new Bundle();
//                parameters.putString("fields", "id,name,email");
//                request.setParameters(parameters);
//                request.executeAsync();
//
//                activity.startActivity(new Intent(activity, MainActivity.class));
//                activity.finish();
//            }
//
//            @Override
//            public void onCancel() {
//                // App code
//            }
//
//            @Override
//            public void onError(FacebookException exception) {
//                // App code
//            }
//        };
//    }
//
//    public static void writeProfileToPrefs(JSONObject jsonObject, Context context) {
//        //    facebook
//        try {
//            String email = jsonObject.getString("email");
//            String name = jsonObject.getString("name");
//            String id = jsonObject.getString("id");
//            String photoUrl = "https://graph.facebook.com/" + id + "/picture?type=large";
//
//            context.getSharedPreferences(ProfileFragment.USER, MODE_PRIVATE).edit()
//                    .putString("username", name)
//                    .putString("displayName", name)
//                    .putString("email", email)
//                    .putString("photoUrl", photoUrl)
//                    .putString("id", id)
//                    .putString(AccountType.getName(), AccountType.FACEBOOK.toString())
//                    .apply();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void signOutFacebook(Context context) {
//        LoginManager.getInstance().logOut();
//        Toast.makeText(context, "Logged Out from facebook",
//                Toast.LENGTH_SHORT).show();
//        Intent i = new Intent(context, MainActivity.class);
//        context.startActivity(i);
//        changePrefsToGuest(context);
//    }
//
//    private static void changePrefsToGuest(Context context) {
//        context.getSharedPreferences(ProfileFragment.USER, MODE_PRIVATE)
//                .edit().putString("username", "guest").apply();
//    }
//}
