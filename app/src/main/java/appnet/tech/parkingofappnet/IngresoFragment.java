package appnet.tech.parkingofappnet;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallbcak;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IngresoFragment extends Fragment {

    View ingreso;
    EditText etpatente;
    TextView tventrada;
    Button btningresar, btnvolver;
    ConexionSQLiteHelper conne;
    String currentDate,currentTime;
    ArrayList<Vehiculo> listaVehiculos = new ArrayList<>();

    SunmiPrinterService sunmiPrinterService;
    InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback() {
        @Override
        protected void onConnected(SunmiPrinterService service) {
            sunmiPrinterService = service;
        }

        @Override
        protected void onDisconnected() {
        }
    };

    String token;
    InnerResultCallbcak innerResultCallbcak = new InnerResultCallbcak() {
        @Override
        public void onRunResult(boolean isSuccess) throws RemoteException {

        }

        @Override
        public void onReturnString(String result) throws RemoteException {

        }

        @Override
        public void onRaiseException(int code, String msg) throws RemoteException {

        }

        @Override
        public void onPrintResult(int code, String msg) throws RemoteException {

        }
    };
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ingreso = inflater.inflate(R.layout.fragment_ingreso, container, false);
        conne = new ConexionSQLiteHelper(getContext(), "bd_appnet_parking", null, 1);

        try {
            InnerPrinterManager.getInstance().bindService(getContext(), innerPrinterCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        etpatente = ingreso.findViewById(R.id.etpatente);
        btningresar = ingreso.findViewById(R.id.btnentrada);
        btnvolver = ingreso.findViewById(R.id.btnvolver);
        tventrada = ingreso.findViewById(R.id.tventrada);

        cargarVehiculos();
        consultarToken();
        //currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        currentDate = sdf.format(c.getTime());
        currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        tventrada.setText("Fecha:" + currentDate.replace(" ","\n") );

        btningresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etpatente.getText().length()>0)
                {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String patente = etpatente.getText().toString();
                            boolean existe = false;

                            for (int i = 0; i < listaVehiculos.size(); i++) {

                                if(listaVehiculos.get(i).getPatente().equals(patente))
                                {
                                    existe=true;
                                    break;
                                }

                            }

                            if(existe)
                            {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(),"Patente ya esta registrada",Toast.LENGTH_LONG).show();
                                    }
                                });
                                return;
                            }

                            JSONObject jsonentrada = new JSONObject();
                            try {
                                jsonentrada.put("patente",patente);
                                jsonentrada.put("fecha_ingreso",currentDate);
                                jsonentrada.put("hora_ingreso",currentTime);
                                jsonentrada.put("estado",1);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            String resp = POST(Utilidades.URL_ENTRADA,jsonentrada.toString(),token);

                            if(resp.toUpperCase().contains("OK"))
                            {
                                SQLiteDatabase db = conne.getWritableDatabase();
                                try {
                                    String insert = "insert into "
                                            + Utilidades.TABLA_VEHICULOS + " ("
                                            + Utilidades.CAMPO_PATENTE + " ,"
                                            + Utilidades.CAMPO_FECHAENTRADA + " ,"
                                            + Utilidades.CAMPO_HORAENTRADA
                                            + ") VALUES ('"
                                            + patente + "','"
                                            + currentDate + "','"
                                            + currentTime + "')";
                                    db.execSQL(insert);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                db.close();

                                imprimirTicket(patente,currentTime,currentDate);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(),"Vehiculo Ingresado",Toast.LENGTH_LONG).show();
                                    }
                                });

                                ((MainActivity) getActivity()).cambioFragment(1);
                            }else {

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(),"Ocurrio un error",Toast.LENGTH_LONG).show();
                                    }
                                });

                            }



                        }
                    });

                    thread.start();



                }else
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"Debe Ingresar una Patente",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        btnvolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).cambioFragment(1);
            }
        });

        return ingreso;
    }

    private void consultarToken() {

        SQLiteDatabase db = conne.getReadableDatabase();

        Cursor cursor = db.rawQuery("select " + Utilidades.CAMPO_TOKENCONF + " from " + Utilidades.TABLA_CONFIGURACION, null);
        cursor.moveToFirst();
        token = cursor.getString(0);

        db.close();
        cursor.close();


    }

    private void imprimirTicket(String patente, String currentTime, String currentDate) {

        try {
            SQLiteDatabase db = conne.getReadableDatabase();
            Cursor cursor = db.rawQuery("select " + Utilidades.CAMPO_RUTEMPRESACONF + "," + Utilidades.CAMPO_RAZONSOCIALCONF + "," + Utilidades.CAMPO_GIROCONF + "," + Utilidades.CAMPO_DIRECCIONCONF + "," + Utilidades.CAMPO_LOGOCONF + "," + Utilidades.CAMPO_RESOLUCIONSIICONF + "," + Utilidades.CAMPO_UNIDADSIICONF  + "," + Utilidades.CAMPO_NOMBREUSERCONF+ " from " + Utilidades.TABLA_CONFIGURACION, null);
            cursor.moveToNext();

            sunmiPrinterService.setAlignment(1, innerResultCallbcak);
            sunmiPrinterService.setFontSize(30,innerResultCallbcak);

            sunmiPrinterService.printText("TICKET DE ESTACIONAMIENTO\n",innerResultCallbcak);
            sunmiPrinterService.setFontSize(25,innerResultCallbcak);

            sunmiPrinterService.printText(cursor.getString(1)+"\n",innerResultCallbcak);
            sunmiPrinterService.printText(cursor.getString(0)+"\n",innerResultCallbcak);

            sunmiPrinterService.printText( "Fecha: "+currentDate+"\n", innerResultCallbcak);
            sunmiPrinterService.printText( "Hora: "+currentTime+"\n", innerResultCallbcak);
            sunmiPrinterService.printText("Usuario: "+cursor.getString(7)+"\n\n",innerResultCallbcak);
            sunmiPrinterService.setFontSize(28,innerResultCallbcak);
            sunmiPrinterService.printText( "Patente: "+patente+"\n\n", innerResultCallbcak);
            sunmiPrinterService.printQRCode(patente,10,1,innerResultCallbcak);
            sunmiPrinterService.setFontSize(19,innerResultCallbcak);
            sunmiPrinterService.printText( "\n", innerResultCallbcak);
            sunmiPrinterService.printText( "Desarrollado por AppnetTech"+"\n", innerResultCallbcak);
            sunmiPrinterService.autoOutPaper(innerResultCallbcak);
            //sunmiPrinterService.printText( "Documento: "+spdoc.getSelectedItem().toString()+"\n", innerResultCallbcak);
            //sunmiPrinterService.printText( "Folio: "+folio+"\n", innerResultCallbcak);
            //sunmiPrinterService.printText( "valido por 30 dias"+"\n", innerResultCallbcak);
            //sunmiPrinterService.printText( "desde la emision del documento"+"\n", innerResultCallbcak);

            //JSONObject jsonObject = new JSONObject();
            //jsonObject.put("tipodoc",numdoc);
            //jsonObject.put("folio",folio);





            //sunmiPrinterService.setFontSize(25,innerResultCallbcak);
//
//
            //sunmiPrinterService.printText("\n\n\n\n",innerResultCallbcak);
            //sunmiPrinterService.cutPaper(innerResultCallbcak);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public String POST(String URL, String rbody, String token) {
        try {
            RequestBody body = RequestBody.create(JSON, rbody);
            Request request = new Request.Builder()
                    .addHeader("token", token)
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

    private void  cargarVehiculos()
    {
        SQLiteDatabase db = conne.getReadableDatabase();
        Cursor cursor = db.rawQuery("select "+ Utilidades.CAMPO_PATENTE+" , "+Utilidades.CAMPO_HORAENTRADA+" , "+Utilidades.CAMPO_FECHAENTRADA+"  from " + Utilidades.TABLA_VEHICULOS, null);

        while (cursor.moveToNext())
        {
            try
            {
                Vehiculo v = new Vehiculo();
                v.setPatente(cursor.getString(0));
                v.setFechaentrada(cursor.getString(2));
                v.setHoraentrada(cursor.getString(1));

                listaVehiculos.add(v);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        db.close();
        cursor.close();
    }

}