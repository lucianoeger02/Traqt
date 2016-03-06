package com.catteno.traqt;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.catteno.traqt.adapters.SessionsAdapter;
import com.catteno.traqt.helpers.IntentExtras;
import com.catteno.traqt.model.DataStore;
import com.catteno.traqt.model.entities.TraqtActivity;
import com.catteno.traqt.model.entities.TraqtSession;
import com.catteno.traqt.model.repositories.SessionRepository;
import com.catteno.utils.Duration;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

public class DetailsActivity
        extends AppCompatActivity
        implements IntentExtras {

    //
    // -- CAMPOS

    private int traqtActivityId;
    private SessionsAdapter sessionsAdapter;
    private RecyclerView historyRecyclerView;
    private TextView noHistoryTextView;
    private ImageView categoryImageView;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private TextView maxRepetitionsTextView;
    private TextView timeLimitTextView;
    private TextView remindersTextView;

    //
    // -- CICLO DE VIDA DA ATIVIDADE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailsActivity.this, SessionActivity.class);
                intent.putExtra(ACTIVITY_ID_EXTRA, traqtActivityId);
                startActivityForResult(intent, REQUEST_SESSION);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializa e Configura os elementos
        nameTextView = (TextView)findViewById(R.id.name_text_view);
        descriptionTextView = (TextView)findViewById(R.id.description_text_view);
        categoryImageView = (ImageView)findViewById(R.id.category_image_view);
        maxRepetitionsTextView = (TextView)findViewById(R.id.max_repetitions_text_view);
        timeLimitTextView = (TextView)findViewById(R.id.time_limit_text_view);
        noHistoryTextView = (TextView)findViewById(R.id.no_history_text_view);
        remindersTextView = (TextView)findViewById(R.id.reminders_text_view);

        // Extrai os extras do Intent para obter o código da Atividade Traqt selecionada
        traqtActivityId = getIntent().getIntExtra(ACTIVITY_ID_EXTRA, 0);
        refreshData();

        sessionsAdapter = new SessionsAdapter(traqtActivityId);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        historyRecyclerView = (RecyclerView)findViewById(R.id.history_recycler_view);
        historyRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerView.setAdapter(sessionsAdapter);
        checkHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // "Infla" o menu adicionando seus items ao ActionBar
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, FormActivity.class);
            intent.putExtra(ACTIVITY_ID_EXTRA, traqtActivityId);
            startActivityForResult(intent, REQUEST_FORM);
        }
        else if (id == R.id.action_delete) {
            // Cria um AlertDialog para confirmar a exclusão dessa atividade
            new AlertDialog.Builder(this)
                    .setMessage("Excluir a atividade? Todo o histórico será removido.")
                    .setPositiveButton("sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Exclui essa atividade
                            clearHistory();
                            TraqtActivity traqtActivity = DataStore.getInstance().getActivityRepository().findById(traqtActivityId);
                            DataStore.getInstance().getActivityRepository().delete(traqtActivity);

                            // Informa a Activity anterior sobre a exclusão deste registro
                            setResult(RESULT_DELETED);
                            finish();

                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        else if (id == R.id.action_clear_history) {
            // Cria um AlertDialog para confirmar a exclusão do histórico.
            new AlertDialog.Builder(this)
                    .setMessage("Limpar o histórico de sessões dessa atividade?")
                    .setPositiveButton("sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearHistory();

                            // Informa ao Activity anterior que esse registro sofreu atualização.
                            setResult(RESULT_UPDATED);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("no", null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FORM) {
            // Caso um registro tenha sido inserido atualiza o Spinner
            if (resultCode == RESULT_UPDATED)
                refreshData();
        } else if (requestCode == REQUEST_SESSION) {
            if (resultCode == RESULT_OK) {
                sessionsAdapter.refreshData();
                checkHistory();
            }
        }
    }

    //
    // -- MÉTODOS PRIVADOS

    void refreshData() {
        TraqtActivity selectedActivity = DataStore.getInstance().getActivityRepository().findById(traqtActivityId);

        if (selectedActivity != null) {
            // Carrega os dados da atividade
            nameTextView.setText(selectedActivity.getName());
            descriptionTextView.setText(selectedActivity.getDescription());
            maxRepetitionsTextView.setText(String.valueOf(selectedActivity.getRepetitions() + " repetições"));
            timeLimitTextView.setText(String.valueOf(new Duration(selectedActivity.getTimeLimit()).getFormattedDuration()));
            categoryImageView.setImageResource(selectedActivity.getCategoryInfo().getIconResourceId());
            remindersTextView.setText(getReminderDescription());
            getSupportActionBar().setTitle(selectedActivity.getName());
        } else {
            // O ID passado é inválido, encerra a atividade retornando a anterior
            finish();
        }
    }

    /**
     * Verifica se há registros no histórico e se deve exibir um label informativo para o usuário
     */
    void checkHistory() {
        if (sessionsAdapter.getItemCount() == 0)
            noHistoryTextView.setVisibility(View.VISIBLE);
        else
            noHistoryTextView.setVisibility(View.GONE);
    }

    /**
     * Limpa o histórico de sessões da atividade
     */
    void clearHistory() {
        SessionRepository repo = DataStore.getInstance().getSessionRepository();
        for (TraqtSession session : repo.findWhere(TraqtSession.COLUMN_ACTIVITY_ID + " = ?", String.valueOf(traqtActivityId))) {
            repo.delete(session);
        }
        sessionsAdapter.refreshData();
    }

    /**
     * Constroi a descrição dos Lembretes.
     * @return Uma string representando as opções de lembrete determinadas pelo usuário.
     */
    String getReminderDescription() {
        TraqtActivity selectedActivity = DataStore.getInstance().getActivityRepository().findById(traqtActivityId);
        if (selectedActivity.isRemindOnSaturday() ||
                selectedActivity.isRemindOnMonday() ||
                selectedActivity.isRemindOnTuesday() ||
                selectedActivity.isRemindOnWednesday() ||
                selectedActivity.isRemindOnThursday() ||
                selectedActivity.isRemindOnFriday() ||
                selectedActivity.isRemindOnSaturday()) {

            StringBuilder reminderBuilder = new StringBuilder();
            boolean isFirst = true;
            if (selectedActivity.isRemindOnSaturday()) {
                reminderBuilder.append("Dom");
                isFirst = false;
            }
            if (selectedActivity.isRemindOnMonday()) {
                if (!isFirst) reminderBuilder.append(", ");
                reminderBuilder.append("Seg");
                isFirst = false;
            }
            if (selectedActivity.isRemindOnTuesday()) {
                if (!isFirst) reminderBuilder.append(", ");
                reminderBuilder.append("Ter");
                isFirst = false;
            }
            if (selectedActivity.isRemindOnWednesday()) {
                if (!isFirst) reminderBuilder.append(", ");
                reminderBuilder.append("Qua");
                isFirst = false;
            }
            if (selectedActivity.isRemindOnThursday()) {
                if (!isFirst) reminderBuilder.append(", ");
                reminderBuilder.append("Qui");
                isFirst = false;
            }
            if (selectedActivity.isRemindOnFriday()) {
                if (!isFirst) reminderBuilder.append(", ");
                reminderBuilder.append("Sex");
                isFirst = false;
            }
            if (selectedActivity.isRemindOnSaturday()) {
                if (!isFirst) reminderBuilder.append(", ");
                reminderBuilder.append("Sab");
            }

            reminderBuilder.append("\n");
            reminderBuilder.append(StringUtils.leftPad(String.valueOf(selectedActivity.getReminderHour()), 2, '0'));
            reminderBuilder.append(":");
            reminderBuilder.append(StringUtils.leftPad(String.valueOf(selectedActivity.getReminderMinute()), 2, '0'));

            return reminderBuilder.toString();
        } else {
            return "não";
        }
    }

}
