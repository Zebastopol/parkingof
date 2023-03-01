package appnet.tech.parkingofappnet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallbcak;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResumenFragment extends Fragment {
    View resumen;
    RecyclerView rvvehiculos;
    AdapterResumen adapterResumen;
    Button btnagregar,btnsalir,btnbuscar,btninforme;
    ArrayList<Vehiculo> listaVehiculos= new ArrayList<>();
    ArrayList<Vehiculo> listaVehiculosTotales= new ArrayList<>();
    ConexionSQLiteHelper conne;
    EditText etbuscador;
    ImageButton btnscanqr;
    SwipeRefreshLayout srlresumen;
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

    String token,nombreuser;
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
        // Inflate the layout for this fragment
        resumen=inflater.inflate(R.layout.fragment_resumen, container, false);
        conne = new ConexionSQLiteHelper(getContext(), "bd_appnet_parking", null, 1);

        try {
            InnerPrinterManager.getInstance().bindService(getContext(), innerPrinterCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnagregar=resumen.findViewById(R.id.btningresar);
        btnsalir=resumen.findViewById(R.id.btnsalir);
        btnbuscar=resumen.findViewById(R.id.btnbuscar);
        etbuscador=resumen.findViewById(R.id.etpatente);
        btnscanqr = resumen.findViewById(R.id.btnescanearqr);
        btninforme=resumen.findViewById(R.id.btninforme);
        srlresumen=resumen.findViewById(R.id.srlresumen);

        srlresumen.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        cargarVehiculos();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapterResumen.notifyDataSetChanged();
                                srlresumen.setRefreshing(false);

                            }
                        });

                    }
                });
                thread.start();
            }
        });


        consultarToken();

        rvvehiculos = (RecyclerView) resumen.findViewById(R.id.rvresumen);
        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(resumen.getContext(), 1);
        rvvehiculos.setLayoutManager(gridLayoutManager1);// set LayoutManager to RecyclerView
        adapterResumen = new AdapterResumen(resumen.getContext(), listaVehiculos);
        rvvehiculos.setAdapter(adapterResumen); // set the Adapter to RecyclerView

        btnbuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaVehiculos.removeAll(listaVehiculos);
                String patente="";

                if (etbuscador.getText().length()>0)
                {
                    patente= etbuscador.getText().toString();
                    for (int i = 0; i < listaVehiculosTotales.size(); i++) {
                        if (listaVehiculosTotales.get(i).getPatente().contains(patente))
                        {
                            listaVehiculos.add(listaVehiculosTotales.get(i));
                        }
                    }
                }else{
                    listaVehiculos.addAll(listaVehiculosTotales);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterResumen.notifyDataSetChanged();
                    }
                });
            }
        });

        btnagregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MainActivity)getActivity()).cambioFragment(2);

            }
        });

        btnsalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert2 = new AlertDialog.Builder(getActivity());
                alert2.setTitle("Salir");
                alert2.setMessage("Cerrar sesión?");
                alert2.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getActivity().deleteDatabase("bd_appnet_parking");
                        ((MainActivity)getActivity()).cambioFragment(4);
                    }
                });

                alert2.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert2.show();

            }
        });

        btninforme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ProgressDialog progress = ProgressDialog.show(resumen.getContext(), "Cargando",
                        "por favor espere", true);

                Thread thread = new Thread(() -> {

                    try {
                        Date cDate = new Date();
                        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

                        JSONObject jsonObject1 = new JSONObject();
                        JSONArray jsonArray = new JSONArray();
                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put("Fecha", fDate);
                        jsonArray.put(jsonObject);
                        jsonObject1.put("Fechas", jsonArray);

                        String resp = POST(Utilidades.URL_CIERRECAJA,jsonObject1.toString(),token);

                        progress.dismiss();


                        imprimirCierre(resp);


                        //getActivity().getIntent().putExtra("jsoncierre", resp);
                        //((Main2Activity) getActivity()).cambioFragment(6);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

                thread.start();

            }
        });

        btnscanqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MainActivity)getActivity()).iniciarScanner();


            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                cargarVehiculos();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterResumen.notifyDataSetChanged();
                    }
                });

            }
        });

        thread.start();


        return resumen;
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

    private void consultarToken() {

        SQLiteDatabase db = conne.getReadableDatabase();

        Cursor cursor = db.rawQuery("select " + Utilidades.CAMPO_TOKENCONF +","+Utilidades.CAMPO_NOMBREUSERCONF+ " from " + Utilidades.TABLA_CONFIGURACION, null);
        cursor.moveToFirst();
        token = cursor.getString(0);
        nombreuser= cursor.getString(1);
        db.close();
        cursor.close();


    }


    private void imprimirCierre(String datos) {
        try {
            Date cDate = new Date();
            String fDate = new SimpleDateFormat("dd-MM-yyyy").format(cDate);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());
            JSONObject dat = new JSONObject(datos);

            JSONObject consolidado = dat.getJSONObject("Consolidado");

            sunmiPrinterService.setAlignment(1, innerResultCallbcak);
            sunmiPrinterService.setFontSize(28, innerResultCallbcak);
            sunmiPrinterService.printText("Cierre de caja" + "\n", innerResultCallbcak);
            sunmiPrinterService.printText("Fecha:" + fDate + "\n", innerResultCallbcak);
            sunmiPrinterService.printText("Hora:" + currentDateandTime + "\n", innerResultCallbcak);
            sunmiPrinterService.printText("Usuario:" + nombreuser + "\n", innerResultCallbcak);
            JSONArray formas = dat.getJSONArray("FormaPago");
            JSONArray docs = dat.getJSONArray("Documentos");
            //JSONArray productos= dat.getJSONArray("Producto");
            DecimalFormat formatter = new DecimalFormat("#,###,###");

            sunmiPrinterService.printText("\n", innerResultCallbcak);
            sunmiPrinterService.setFontSize(28, innerResultCallbcak);

            String[] textos;

            int[] ancho = new int[2];
            ancho[0] = 20;
            ancho[1] = 11;

            int[] align = new int[2];
            align[0] = 0;
            align[1] = 2;


            sunmiPrinterService.setFontSize(25, innerResultCallbcak);
            sunmiPrinterService.printText("\n", innerResultCallbcak);

            textos = new String[3];
            textos[0] = "Forma";
            textos[1] = "Cant";
            textos[2] = "Total";

            ancho = new int[3];
            ancho[0] = 10;
            ancho[1] = 10;
            ancho[2] = 11;

            align = new int[3];
            align[0] = 0;
            align[1] = 1;
            align[2] = 2;

            try {
                sunmiPrinterService.printColumnsString(textos, ancho, align, innerResultCallbcak);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            sunmiPrinterService.printText("-----------------------------" + "\n", innerResultCallbcak);

            for (int i = 0; i < formas.length(); i++) {

                textos = new String[3];
                textos[0] = formas.getJSONObject(i).getString("nombre");
                textos[1] = formas.getJSONObject(i).getString("count");
                textos[2] = "$" + formatter.format(formas.getJSONObject(i).getInt("total"));

                ancho = new int[3];
                ancho[0] = 10;
                ancho[1] = 10;
                ancho[2] = 11;

                align = new int[3];
                align[0] = 0;
                align[1] = 1;
                align[2] = 2;

                try {
                    sunmiPrinterService.printColumnsString(textos, ancho, align, innerResultCallbcak);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            sunmiPrinterService.printText("-----------------------------" + "\n", innerResultCallbcak);
            sunmiPrinterService.printText("\n", innerResultCallbcak);

            textos = new String[3];
            textos[0] = "Documento";
            textos[1] = "Cant";
            textos[2] = "Total";

            ancho = new int[3];
            ancho[0] = 10;
            ancho[1] = 10;
            ancho[2] = 11;

            align = new int[3];
            align[0] = 0;
            align[1] = 1;
            align[2] = 2;

            try {
                sunmiPrinterService.printColumnsString(textos, ancho, align, innerResultCallbcak);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            sunmiPrinterService.printText("-----------------------------" + "\n", innerResultCallbcak);

            for (int i = 0; i < docs.length(); i++) {

                textos = new String[3];
                textos[0] = docs.getJSONObject(i).getString("nombre");
                textos[1] = docs.getJSONObject(i).getString("count");
                textos[2] = "$" + formatter.format(docs.getJSONObject(i).getInt("total"));

                ancho = new int[3];
                ancho[0] = 10;
                ancho[1] = 10;
                ancho[2] = 11;

                align = new int[3];
                align[0] = 0;
                align[1] = 1;
                align[2] = 2;

                try {
                    sunmiPrinterService.printColumnsString(textos, ancho, align, innerResultCallbcak);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            sunmiPrinterService.printText("-----------------------------" + "\n", innerResultCallbcak);
            sunmiPrinterService.printText("\n", innerResultCallbcak);
            sunmiPrinterService.setFontSize(28, innerResultCallbcak);

            textos = new String[2];
            textos[0] = "Total:";
            textos[1] = "$" + formatter.format(consolidado.getInt("Total"));

            ancho = new int[2];
            ancho[0] = 20;
            ancho[1] = 11;

            align = new int[2];
            align[0] = 0;
            align[1] = 2;

            sunmiPrinterService.printColumnsString(textos, ancho, align, innerResultCallbcak);
            sunmiPrinterService.printText("\n", innerResultCallbcak);

            sunmiPrinterService.printText("\n", innerResultCallbcak);
            sunmiPrinterService.cutPaper(innerResultCallbcak);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void  cargarVehiculos()
    {

        try {

            listaVehiculos.clear();
            listaVehiculosTotales.clear();

            String resp = GET(Utilidades.URL_REGISTRO,token);

            JSONArray jsonArray = new JSONArray(resp);

            for (int i = 0; i < jsonArray.length() ; i++) {

                try
                {
                    int estado = jsonArray.getJSONObject(i).getInt("estado");
                    if (estado ==1)
                    {
                        Vehiculo v = new Vehiculo();
                        v.setPatente(jsonArray.getJSONObject(i).getString("patente"));
                        v.setFechaentrada(jsonArray.getJSONObject(i).getString("fecha_ingreso"));
                        v.setHoraentrada(jsonArray.getJSONObject(i).getString("hora_ingreso"));
                        listaVehiculos.add(v);
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            listaVehiculosTotales.addAll(listaVehiculos);


        }catch (Exception e)
        {
            e.printStackTrace();
        }

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

    public void cargarTicket(String patente)
    {
        Vehiculo ve= new Vehiculo();
        boolean existe = false;

        for (int i = 0; i < listaVehiculosTotales.size(); i++) {
            if(listaVehiculosTotales.get(i).getPatente().equals(patente))
            {
                existe=true;
                ve=listaVehiculosTotales.get(i);
                break;
            }
        }

        if(!existe)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(),"Patente no registrada",Toast.LENGTH_LONG).show();
                }
            });
        }else
        {
            getActivity().getIntent().putExtra("vehiculo",ve);
            ((MainActivity)getActivity()).cambioFragment(3);
        }
    }
}