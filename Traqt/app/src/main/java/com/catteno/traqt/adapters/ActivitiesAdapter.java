package com.catteno.traqt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.catteno.traqt.R;
import com.catteno.traqt.model.entities.TraqtActivity;
import com.catteno.traqt.model.entities.TraqtSession;

/**
 * Um Adapter para exibir Atividades do Traqt usando o layout de item 'list_item_activity'.
 */
public class ActivitiesAdapter extends ArrayAdapter<TraqtActivity> {

    private final Context context;
    private final TraqtActivity[] values;

    public ActivitiesAdapter(Context context, TraqtActivity[] values) {
        super(context, 0, values);

        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // Primeiro verificamos se uma View criada anteriormente pode ser re-aproveitada
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_activity, parent, false);

            // Cria um novo "Holder" para a View
            viewHolder = new ViewHolder();
            viewHolder.categoryImageView = (ImageView)convertView.findViewById(R.id.category_image_view);
            viewHolder.nameTextView = (TextView)convertView.findViewById(R.id.name_text_view);
            viewHolder.detailsTextView = (TextView)convertView.findViewById(R.id.details_text_view);

            // Salva o "Holder" dentro da própria View para ser re-aproveitado.
            convertView.setTag(viewHolder);
        } else {
            // Obtém o Holder associado a essa View
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // Configuramos a View da célula
        TraqtActivity activity = values[position];

        viewHolder.nameTextView.setText(activity.getName());
        if (activity.getHistory().size() == 0) {
            viewHolder.detailsTextView.setText("Nenhuma sessão.");
        } else {
            TraqtSession session = activity.getHistory().get(activity.getHistory().size() - 1);
            viewHolder.detailsTextView.setText("Última sessão em " + session.getFormattedStartTime());
        }
        viewHolder.categoryImageView.setImageResource(activity.getCategoryInfo().getIconResourceId());

        return convertView;
    }

    private static class ViewHolder {

        public ImageView categoryImageView;
        public TextView nameTextView;
        public TextView detailsTextView;

    }

}
