package appnet.tech.parkingofappnet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpLogin;
import com.sumup.merchant.reader.api.SumUpPayment;
import com.sumup.merchant.reader.api.SumUpState;
import com.sumup.merchant.reader.models.Merchant;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    LoginFragment loginFragment;
    IngresoFragment ingresoFragment;
    ResumenFragment resumenFragment;
    SalidaFragment salidaFragment;
    int selectedfragment;
    String txcode;
    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private Class<?> mClss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trustEveryone();

        loginFragment = new LoginFragment();

        SumUpState.init(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.contenedor, loginFragment).commit();

    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

    public void loginSumup() {
        //SumUpLogin sumupLogin = SumUpLogin.builder("8518a6a6-4448-4ba9-a87c-090fc72cb322").build();


        SumUpLogin sumupLogin = SumUpLogin.builder("72109315-cd98-4c33-b1f7-7afd250e1aaa").build();

        SumUpAPI.openLoginActivity(MainActivity.this, sumupLogin, 1);
    }


    public void generarsumup(final int total) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (!SumUpAPI.isLoggedIn()) {
                        loginSumup();
                        // no merchant account currently logged in
                        return;
                    } else {
                        Merchant currentMerchant = SumUpAPI.getCurrentMerchant();
                    }
                    SumUpPayment payment = SumUpPayment.builder()
                            // mandatory parameters
                            .total(new BigDecimal(total)) // minimum 1.00
                            .currency(SumUpPayment.Currency.CLP)
                            // optional: include a tip amount in addition to the total
                            // optional: add details
                            //.title("Parking")
                            //.receiptEmail("customer@mail.com")
                            //.receiptSMS("+56900000000")
                            //// optional: Add metadata
                            //.addAdditionalInfo("AccountId", "taxi0334")
                            //.addAdditionalInfo("From", "Paris")
                            //.addAdditionalInfo("To", "Berlin")
                            // optional: foreign transaction ID, must be unique!
                            // optional: skip the success screen
                            .skipSuccessScreen()
                            // optional: skip the failed screen
                            .skipFailedScreen()
                            .build();

                    SumUpAPI.checkout(MainActivity.this, payment, 2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public void cambioFragment(int i) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (i) {
            case 1:
                selectedfragment = 1;
                resumenFragment = new ResumenFragment();
                fragmentManager.beginTransaction().replace(R.id.contenedor, resumenFragment).commit();
                break;
            case 2:
                selectedfragment = 2;
                ingresoFragment = new IngresoFragment();
                fragmentManager.beginTransaction().replace(R.id.contenedor, ingresoFragment).commit();
                break;
            case 3:
                selectedfragment = 3;
                salidaFragment = new SalidaFragment();
                fragmentManager.beginTransaction().replace(R.id.contenedor, salidaFragment).commit();
                break;
            case 4:
                selectedfragment = 4;
                loginFragment = new LoginFragment();
                fragmentManager.beginTransaction().replace(R.id.contenedor, loginFragment).commit();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");

                resumenFragment.cargarTicket(contents);
            }
        }

        if (requestCode == 2 && data != null) {
            final String MESSAGE = String.valueOf(data.getExtras().getString(SumUpAPI.Response.MESSAGE));
            txcode = String.valueOf(data.getExtras().getString(SumUpAPI.Response.TX_CODE));
            //boolean issumup=true;

            if (resultCode == 1) {

                salidaFragment.issumup = true;
                salidaFragment.txcode = txcode;
                salidaFragment.btnsalida.callOnClick();

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), MESSAGE, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

    }

    public void iniciarScanner() {

        launchActivity(SimpleScannerActivity.class);

    }

    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZBAR_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivityForResult(intent, 0);
        }
    }


}