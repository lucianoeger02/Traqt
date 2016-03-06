package com.catteno.traqt.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.catteno.traqt.R;
import com.catteno.traqt.model.DataStore;
import com.catteno.traqt.model.entities.TraqtSession;
import com.catteno.utils.Duration;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

/**
 * Um Adapter para o RecyclerView de Histórico de Sessões.
 */
public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {

    // Declara dois objetos static de formatação que usados dentro da classe

    static DateTimeFormatter dateFormat = DateTimeFormat.shortDate();
    static DateTimeFormatter timeFormat = DateTimeFormat.shortTime();

    //
    // -- CAMPOS

    int activityId;                     // O ID da atividade associada a esta instância.
    List<TraqtSession> dataset;         // A lista de registros que esta sendo apresentada.

    //
    // -- CONSTRUTORES

    /**
     * Cria uma instância dessa classe associada a uma atividade do Traqt.
     * @param activityId ID da atividade associada a esse adaptador.
     */
    public SessionsAdapter(int activityId) {
        this.activityId = activityId;
        refreshData();
    }

    //
    // -- API PÚBLICA

    /**
     * Atualiza os dados dessa listagem.
     */
    public void refreshData() {
        dataset = DataStore.getInstance().getSessionRepository().findWhere("activityId = ?", String.valueOf(activityId));
        notifyDataSetChanged();
    }

    //
    // -- OVERRIDES: Sobrescreve os métodos necessários

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Cria uma nova View e associa ela a um objeto ViewHolder
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_session, parent, false);

        ViewHolder vw = new ViewHolder(v);
        return vw;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Obtém o item referente a essa posição dentro do dataset
        TraqtSession session = dataset.get(position);

        // Carrega as informações nos componentes da interface
        holder.startDateTextView.setText(dateFormat.print(session.getStartTime()));
        holder.startTimeTextView.setText(timeFormat.print(session.getStartTime()));
        holder.repetitionsTextView.setText(String.valueOf(session.getTotalReptitions()) + " repetições");
        holder.durationTextView.setText(new Duration(session.getElapsedTime()).getFormattedDuration());
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    //
    // -- TIPOS INTERNOS

    /**
     * Um objeto para armazenar as referências aos componentes de uma View.
     * Esse objeto esta vinculado ao layout 'list_item_session.xml'.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView startDateTextView;
        TextView startTimeTextView;
        TextView repetitionsTextView;
        TextView durationTextView;

        public ViewHolder(View view) {
            super(view);
            startDateTextView = (TextView)view.findViewById(R.id.date_text_view);
            startTimeTextView = (TextView)view.findViewById(R.id.time_text_view);
            repetitionsTextView = (TextView)view.findViewById(R.id.repetitions_text_view);
            durationTextView = (TextView)view.findViewById(R.id.duration_text_view);
        }

    }

}
