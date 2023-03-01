package appnet.tech.parkingofappnet;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginFragment extends Fragment {

    private Button btnlogin;
    private EditText etrut, etpass, etuser;
    View login;
    ConexionSQLiteHelper conne;
    String token, idbodega, rbody, direccion, idcentro, nombrebodega, nombreVendedor, rutvendedor;
    String mensaje = null;
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        login = inflater.inflate(R.layout.fragment_login, container, false);
        conne = new ConexionSQLiteHelper(getContext(), "bd_appnet_parking", null, 1);

        if (consultarbd() == true) {
            ((MainActivity) getActivity()).cambioFragment(1);
        }

        btnlogin = login.findViewById(R.id.btnlogin);
        etuser = login.findViewById(R.id.etuser);
        etrut = login.findViewById(R.id.etrut);
        etpass = login.findViewById(R.id.etpass);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread() {
                    ProgressDialog progressDialog = ProgressDialog.show(getContext(), "Recuperando configuracion",
                            "Cargando, por favor espere", true);

                    @Override
                    public void run() {
                        String etrutstring = etrut.getText().toString();
                        if (etrutstring.contains(".")) {
                            etrutstring.replace(".", "");
                        }
                        if (!etrutstring.substring(etrutstring.length() - 2, etrutstring.length() - 1).equals("-")) {
                            etrutstring = etrutstring.substring(0, etrutstring.length() - 1) + "-" + etrutstring.substring(etrutstring.length() - 1);
                        }
                        org.json.JSONObject jsonBody = new JSONObject();
                        if (etuser.getText().toString().isEmpty() || etpass.getText().toString().isEmpty()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "Complete todos los campos", Toast.LENGTH_LONG).show();
                                }
                            });
                            progressDialog.dismiss();
                            return;
                        }
                        try {
                            jsonBody.put("empresa", etrutstring);
                            jsonBody.put("username", etuser.getText().toString());
                            jsonBody.put("password", etpass.getText().toString());

                            rbody = jsonBody.toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            return;
                        }


                        String config = POST(Utilidades.URL_LOGIN, rbody);

                        Log.d("asdasd", config);
                        try {
                            JSONArray con = new JSONArray(config);
                            mensaje = con.getJSONObject(0).getString("Token");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (mensaje != null) {
                            try {
                                guardarToken(new JSONArray(config));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String conf = "";
                            conf = GET(Utilidades.URL_EMPRESA, token);

                            conf.replace("\\u00f1", "Ñ");


                            String configparking = GET(Utilidades.URL_CONFIGPARKING, token);
                            String sucursales = GET(Utilidades.URL_SUCURSAL, token);
                            String tipos = GET(Utilidades.URL_TIPOPAGO, token);


                            try {
                                rellenarSucursales(new JSONArray(sucursales));
                                rellenarTipos(new JSONArray(tipos));
                                rellenarConfigParking(new JSONObject(configparking));
                                rellenarConfig(new JSONArray(conf));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            getActivity().getIntent().putExtra("token", token);
                            getActivity().getIntent().putExtra("bodega", idbodega);
                            progressDialog.dismiss();

                            ((MainActivity) getActivity()).cambioFragment(1);

                        } else {
                            mensaje = null;
                            progressDialog.dismiss();
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Credenciales incorrectas", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                thread.start();
            }
        });

        return login;
    }

    private Boolean consultarbd() {

        try {
            int i = 0;
            SQLiteDatabase db = conne.getReadableDatabase();

            Cursor cursor = db.rawQuery("select " + Utilidades.CAMPO_TOKENCONF + " from " + Utilidades.TABLA_CONFIGURACION, null);
            cursor.moveToFirst();
            i = cursor.getCount();
            if (i == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void rellenarTipos(JSONArray jsonArray) throws JSONException {
        SQLiteDatabase db = conne.getWritableDatabase();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String insert = "insert into "
                        + Utilidades.TABLA_TIPOPAGO + " ("
                        + Utilidades.CAMPO_IDTIPO + " ,"
                        + Utilidades.CAMPO_EFECTIVOTIPO + " ,"
                        + Utilidades.CAMPO_NOMBRETIPO
                        + ") VALUES ('"
                        + jsonArray.getJSONObject(i).getString("id") + "','"
                        + jsonArray.getJSONObject(i).getString("sii") + "','"
                        + jsonArray.getJSONObject(i).getString("nombre") + "')";
                db.execSQL(insert);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        db.close();
    }

    private void rellenarConfigParking(JSONObject jsonObject) {
        SQLiteDatabase db = conne.getWritableDatabase();
        try {
            String insert = "insert into "
                    + Utilidades.TABLA_PARKING + " ("
                    + Utilidades.CAMPO_VALORCOBROPARKING + " ,"
                    + Utilidades.CAMPO_ESPACIOSPARKING + " ,"
                    + Utilidades.CAMPO_COBROBASEPARKING + " ,"
                    + Utilidades.CAMPO_MINCOBROBASEPARKING + " ,"
                    + Utilidades.CAMPO_TIPOCOBROPARKING
                    + ") VALUES ('"
                    + jsonObject.getString("valor_cobro") + "','"
                    + jsonObject.getString("cantidad_espacios") + "','"
                    + jsonObject.getString("cobro_base") + "','"
                    + jsonObject.getString("min_cobro_base") + "','"
                    + jsonObject.getString("tipo_cobro") + "')";
            db.execSQL(insert);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.close();
    }

    public void rellenarConfig(JSONArray jsonArray) {
        SQLiteDatabase db = conne.getWritableDatabase();
        try {
            String insert = "insert into "
                    + Utilidades.TABLA_CONFIGURACION + " ("
                    + Utilidades.CAMPO_RUTEMPRESACONF + " ,"
                    + Utilidades.CAMPO_BODEGACONF + " ,"
                    + Utilidades.CAMPO_CENTROCONF + " ,"
                    + Utilidades.CAMPO_VOUCHERCONF + " ,"
                    + Utilidades.CAMPO_ESTADOCONF + " ,"
                    + Utilidades.CAMPO_RAZONSOCIALCONF + " ,"
                    + Utilidades.CAMPO_DIRECCIONCONF + " ,"
                    + Utilidades.CAMPO_RESOLUCIONSIICONF + " ,"
                    + Utilidades.CAMPO_UNIDADSIICONF + " ,"
                    + Utilidades.CAMPO_DOCCONF + " ,"
                    + Utilidades.CAMPO_TOKENCONF + " ,"
                    + Utilidades.CAMPO_LISTAPRECIOCONF + " ,"
                    + Utilidades.CAMPO_CATCONF + " ,"
                    + Utilidades.CAMPO_TOKENMACHCONF + " ,"
                    + Utilidades.CAMPO_NOMBREUSERCONF + " ,"
                    + Utilidades.CAMPO_GIROCONF
                    + ") VALUES ('"
                    + jsonArray.getJSONObject(0).getString("rut_empresa") + "','"
                    + idbodega + "','"
                    + idcentro + "','"
                    + 1 + "','"
                    + jsonArray.getJSONObject(0).getString("estado_empresa") + "','"
                    + jsonArray.getJSONObject(0).getString("razon_social") + "','"
                    + direccion + "','"
                    + jsonArray.getJSONObject(0).getString("resolucion_sii") + "','"
                    + jsonArray.getJSONObject(0).getString("unidad_sii") + "','"
                    + 0 + "','"
                    + token + "','"
                    + "base" + "','"
                    + 0 + "','"
                    + jsonArray.getJSONObject(0).getString("token_mach") + "','"
                    + nombreVendedor + "','"
                    + jsonArray.getJSONObject(0).getString("giro") + "')";


            db.execSQL(insert);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.close();
    }

    private String POST(String URL, String rbody) {

        try {
            RequestBody body = RequestBody.create(JSON, rbody);
            Request request = new Request.Builder()
                    //.addHeader("token", token)
                    .url(URL)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Hubo un problema";

    }

    public void guardarToken(JSONArray jsonArray) {
        try {
            token = jsonArray.getJSONObject(0).getString("Token");
            idbodega = jsonArray.getJSONObject(0).getString("id_bodega");
            nombrebodega = jsonArray.getJSONObject(0).getString("bodega");
            nombreVendedor = jsonArray.getJSONObject(0).getString("Nombre");
            rutvendedor = jsonArray.getJSONObject(0).getString("rut");

        } catch (JSONException e) {
            rutvendedor = "Sin rut";
            e.printStackTrace();
        }
    }

    private void rellenarSucursales(JSONArray jsonArray) {

        StringBuilder direccionc = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                direccionc.append(jsonArray.getJSONObject(i).getString("direccion") + "," + jsonArray.getJSONObject(i).getString("comuna") + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        direccion = direccionc.toString();

    }

    public String GET(String URL, String token) {
        try {
            Request request = new Request.Builder()
                    .addHeader("token", token)
                    .url(URL)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Hubo un problema";
    }

    private String convertInputStreamToString(InputStream input) throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
        String line = "";
        String result = "";
        while ((line = buffer.readLine()) != null) {
            result += line;
        }
        input.close();
        return result;
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}