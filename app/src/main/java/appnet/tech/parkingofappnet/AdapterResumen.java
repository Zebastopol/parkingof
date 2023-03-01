package appnet.tech.parkingofappnet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterResumen extends RecyclerView.Adapter<AdapterResumen.MyView> {
    ArrayList<Vehiculo> listaVehiculos;
    Context context;

    public AdapterResumen(Context context, ArrayList<Vehiculo> listaVehiculos) {
        this.context = context;
        this.listaVehiculos = listaVehiculos;
    }

    @NonNull
    @Override
    public MyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new MyView(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyView holder, final int position) {
        holder.tvpatente.setText(listaVehiculos.get(position).getPatente());
        holder.tventrada.setText(listaVehiculos.get(position).getFechaentrada().replace(" ","\n"));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)context).getIntent().putExtra("vehiculo",listaVehiculos.get(position));
                ((MainActivity)context).cambioFragment(3);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaVehiculos.size();
    }

    public class MyView extends RecyclerView.ViewHolder {
        TextView tvpatente, tventrada;
        CardView cardView;

        public MyView(View itemView) {
            super(itemView);
            tvpatente = (TextView) itemView.findViewById(R.id.tvpatente);
            tventrada = (TextView) itemView.findViewById(R.id.tventrada);
            cardView = itemView.findViewById(R.id.cvvehiculo);
        }
    }

}
