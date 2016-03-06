package com.catteno.traqt;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.catteno.traqt.helpers.IntentExtras;
import com.catteno.traqt.model.DataStore;
import com.catteno.traqt.model.entities.TraqtActivity;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class FormActivity
        extends AppCompatActivity
        implements IntentExtras {

    //
    // -- CAMPOS

    int activityId;
    TraqtActivity traqtActivity;
    DataStore dataStore;

    int reminderHour = 8;
    int reminderMinute = 0;

    //
    // -- COMPONENTES DA VIEW

    EditText nameEditText;
    EditText descriptionEditText;
    RadioGroup categoryRadioGroup;
    CheckBox measureRepetitionsCheckbox;
    EditText repetitionsEditText;
    CheckBox vibrateCheckbox;
    CheckBox measureTimeCheckbox;
    EditText timeLimitEditText;
    ToggleButton remindOnSundayToggle;
    ToggleButton remindOnMondayToggle;
    ToggleButton remindOnTuesdayToggle;
    ToggleButton remindOnWednesdayToggle;
    ToggleButton remindOnThursdayToggle;
    ToggleButton remindOnFridayToggle;
    ToggleButton remindOnSaturdayToggle;
    TextView reminderTimeTextView;

    //
    // -- CICLO DE VIDA DA ACTIVITY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializa os campos
        dataStore = DataStore.getInstance();

        // Obtém os elementos
        nameEditText = (EditText)findViewById(R.id.name_edit_text);
        descriptionEditText = (EditText)findViewById(R.id.description_text_view);
        categoryRadioGroup = (RadioGroup)findViewById(R.id.category_radio_group);
        measureRepetitionsCheckbox = (CheckBox)findViewById(R.id.measure_repetitions_checkbox);
        repetitionsEditText = (EditText)findViewById(R.id.repetitions_text_view);
        vibrateCheckbox = (CheckBox)findViewById(R.id.vibrate_checkbox);
        measureTimeCheckbox = (CheckBox)findViewById(R.id.measure_time_checkbox);
        timeLimitEditText = (EditText)findViewById(R.id.time_limit_text_view);
        reminderTimeTextView = (TextView)findViewById(R.id.reminder_time_text_view);
        remindOnSundayToggle = (ToggleButton)findViewById(R.id.remind_sunday_toggle);
        remindOnMondayToggle = (ToggleButton)findViewById(R.id.remind_monday_toggle);
        remindOnTuesdayToggle = (ToggleButton)findViewById(R.id.remind_tuesday_toggle);
        remindOnWednesdayToggle = (ToggleButton)findViewById(R.id.remind_wednesday_toggle);
        remindOnThursdayToggle = (ToggleButton)findViewById(R.id.remind_thursday_toggle);
        remindOnFridayToggle = (ToggleButton)findViewById(R.id.remind_friday_toggle);
        remindOnSaturdayToggle = (ToggleButton)findViewById(R.id.remind_saturday_toggle);

        // Configura os elementos
        measureRepetitionsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                repetitionsEditText.setEnabled(isChecked);
                vibrateCheckbox.setEnabled(isChecked);
                if (isChecked)
                    repetitionsEditText.setText("0");
            }
        });

        measureTimeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                timeLimitEditText.setEnabled(isChecked);
                if (isChecked)
                    timeLimitEditText.setText("0");
            }
        });

        Button pickReminderTimeButton = (Button)findViewById(R.id.pick_reminder_time_button);
        pickReminderTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment pickerFragment = new TimePickerFragment();
                pickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        reminderHour = hourOfDay;
                        reminderMinute = minute;
                        updateReminderTimeDisplay();
                    }
                });
                pickerFragment.show(getFragmentManager(), "timePicker");
            }
        });

        // Verifica se recebeu uma Atividade via Intent, isso indica edição, caso contrário é um novo registro
        activityId = getIntent().getIntExtra(ACTIVITY_ID_EXTRA, 0);
        if (activityId == 0) {
            getSupportActionBar().setTitle("Nova Atividade");
        } else {
            // Tenta carregar a atividade com o ID corresponde
            traqtActivity = dataStore.getActivityRepository().findById(activityId);

            // Caso não encontre o registro finaliza a atividade e informa o usuário
            if (traqtActivity == null) {
                Toast.makeText(this, "Não foi possível carregar a atividade.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Carrega as informações da atividade
            getSupportActionBar().setTitle("Editar Atividade");
            nameEditText.setText(traqtActivity.getName());
            descriptionEditText.setText(traqtActivity.getDescription());
            ((RadioButton)categoryRadioGroup.getChildAt(traqtActivity.getCategory())).setChecked(true);

            int repetitions = traqtActivity.getRepetitions();
            repetitionsEditText.setText(String.valueOf(repetitions));
            measureRepetitionsCheckbox.setChecked(repetitions > 0);

            long timeLimit = TimeUnit.SECONDS.toMinutes(traqtActivity.getTimeLimit());
            timeLimitEditText.setText(String.valueOf(timeLimit));
            measureTimeCheckbox.setChecked(timeLimit > 0);

            vibrateCheckbox.setChecked(traqtActivity.isEnableVibration());
            remindOnSundayToggle.setChecked(traqtActivity.isRemindOnSunday());
            remindOnMondayToggle.setChecked(traqtActivity.isRemindOnMonday());
            remindOnTuesdayToggle.setChecked(traqtActivity.isRemindOnTuesday());
            remindOnWednesdayToggle.setChecked(traqtActivity.isRemindOnWednesday());
            remindOnThursdayToggle.setChecked(traqtActivity.isRemindOnThursday());
            remindOnFridayToggle.setChecked(traqtActivity.isRemindOnFriday());
            remindOnSaturdayToggle.setChecked(traqtActivity.isRemindOnSaturday());

            reminderHour = traqtActivity.getReminderHour();
            reminderMinute = traqtActivity.getReminderMinute();
        }
        updateReminderTimeDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // "Infla" o menu adicionando seus items ao ActionBar
        getMenuInflater().inflate(R.menu.menu_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {

            if (!validateForm())
                return super.onOptionsItemSelected(item);

            // Se for um novo registro cria uma instância
            if (activityId == 0) {
                traqtActivity = new TraqtActivity();
            }

            traqtActivity.setName(nameEditText.getText().toString());
            traqtActivity.setDescription(descriptionEditText.getText().toString());

            // Obtém o índice do Radio selecionado
            int radioButtonID = categoryRadioGroup.getCheckedRadioButtonId();
            View radioButton = categoryRadioGroup.findViewById(radioButtonID);
            int selectedCategory = categoryRadioGroup.indexOfChild(radioButton);
            traqtActivity.setCategory(selectedCategory);

            int repetitions = Integer.valueOf(repetitionsEditText.getText().toString());
            traqtActivity.setRepetitions(repetitions);

            long timeLimit = Long.valueOf(timeLimitEditText.getText().toString());
            timeLimit = TimeUnit.MINUTES.toSeconds(timeLimit);
            traqtActivity.setTimeLimit(timeLimit);
            traqtActivity.setRemindOnSunday(remindOnSundayToggle.isChecked());
            traqtActivity.setRemindOnMonday(remindOnMondayToggle.isChecked());
            traqtActivity.setRemindOnTuesday(remindOnTuesdayToggle.isChecked());
            traqtActivity.setRemindOnWednesday(remindOnWednesdayToggle.isChecked());
            traqtActivity.setRemindOnThursday(remindOnThursdayToggle.isChecked());
            traqtActivity.setRemindOnFriday(remindOnFridayToggle.isChecked());
            traqtActivity.setRemindOnSaturday(remindOnSaturdayToggle.isChecked());
            traqtActivity.setReminderHour(reminderHour);
            traqtActivity.setReminderMinute(reminderMinute);

            // Salva ou atualiza
            if (activityId == 0) {
                activityId = (int)dataStore.getActivityRepository().insert(traqtActivity);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(ACTIVITY_ID_EXTRA, activityId);
                setResult(RESULT_INSERTED, resultIntent);
            } else {
                dataStore.getActivityRepository().update(traqtActivity);
                setResult(RESULT_UPDATED);
            }

            // Processa o agendamento de lembretes
            processScheduleReminders();

            // Informa o usuário e retorna para a tela anterior
            Toast.makeText(this, "Registro salvo com sucesso!", Toast.LENGTH_LONG).show();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Apresenta uma confirmação para finalizar essa Activity descartando as alterações do formulário
        new AlertDialog.Builder(this)
                .setTitle("Deseja descartar suas alterações?")
                .setNegativeButton("não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    void updateReminderTimeDisplay() {
        String hours = StringUtils.leftPad(String.valueOf(reminderHour), 2, '0');
        String minutes = StringUtils.leftPad(String.valueOf(reminderMinute), 2, '0');
        String time = hours + ":" + minutes;
        reminderTimeTextView.setText(time);
    }

    /**
     * Gera um código para notificação baseado em um código base (codigo da atividade) e no dia da semana.
     * @param baseId Código base da notificação.
     * @param weekDay Número do dia da semana.
     * @return Um código único para essa notificação.
     */
    int getReminderCode(int baseId, int weekDay) {
        return (weekDay * 100000) + baseId;
    }

    /**
     * Remove todas as notificação agendadas para um código base.
     * @param baseId Código base.
     */
    void clearSchedules(int baseId) {
        // O Intent de notificação
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        // Obtém o AlarmManager e passa pelos dias da semana removendo as notificações agendadas para esse código base
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            int code = getReminderCode(baseId, i);
            PendingIntent broadcast = PendingIntent.getBroadcast(this, code, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(broadcast);
        }
    }

    /**
     * Obtém a próxima data para o dia da semana selecionado a partir da data de referência.
     * @param refDate Data de referência.
     * @param weekday Dia da semana alvo.
     * @return A data do próximo dia da semana alvo.
     */
    Calendar getNextDateForWeekday(Calendar refDate, int weekday) {
        while (refDate.get(Calendar.DAY_OF_WEEK) != weekday) {
            refDate.add(Calendar.DATE, 1);
        }
        return refDate;
    }

    /**
     * Agenda um lembrete para o dia da semana da e horários passados.
     * @param weekday Dia da semana.
     * @param hour Horário do dia.
     * @param minute Minuto do horário.
     * @param baseId O código base para identificação desse lembrete.
     */
    void scheduleReminder(int weekday, int hour, int minute, int baseId) {
        // Obtém o servio de AlarmManager para agendar o lembrete
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        // Configura o alarme recorrente
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        int code = getReminderCode(baseId, weekday);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, code, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = getNextDateForWeekday(Calendar.getInstance(), weekday);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), (AlarmManager.INTERVAL_DAY * 7), broadcast);
    }

    /**
     * Processa o agendamento dos lembretes dessa atividade.
     */
    void processScheduleReminders() {
        // Limpa os agendamentos anteriores
        clearSchedules(traqtActivity.getId());

        // Checa os dias em que foi solicitado lembrete e agenda os lembretes
        if (traqtActivity.isRemindOnSunday()) {
            scheduleReminder(Calendar.SUNDAY, traqtActivity.getReminderHour(), traqtActivity.getReminderMinute(), traqtActivity.getId());
        }
        if (traqtActivity.isRemindOnMonday()) {
            scheduleReminder(Calendar.MONDAY, traqtActivity.getReminderHour(), traqtActivity.getReminderMinute(), traqtActivity.getId());
        }
        if (traqtActivity.isRemindOnTuesday()) {
            scheduleReminder(Calendar.TUESDAY, traqtActivity.getReminderHour(), traqtActivity.getReminderMinute(), traqtActivity.getId());
        }
        if (traqtActivity.isRemindOnWednesday()) {
            scheduleReminder(Calendar.WEDNESDAY, traqtActivity.getReminderHour(), traqtActivity.getReminderMinute(), traqtActivity.getId());
        }
        if (traqtActivity.isRemindOnThursday()) {
            scheduleReminder(Calendar.THURSDAY, traqtActivity.getReminderHour(), traqtActivity.getReminderMinute(), traqtActivity.getId());
        }
        if (traqtActivity.isRemindOnFriday()) {
            scheduleReminder(Calendar.FRIDAY, traqtActivity.getReminderHour(), traqtActivity.getReminderMinute(), traqtActivity.getId());
        }
        if (traqtActivity.isRemindOnSaturday()) {
            scheduleReminder(Calendar.SATURDAY, traqtActivity.getReminderHour(), traqtActivity.getReminderMinute(), traqtActivity.getId());
        }
    }

    /**
     * Valida o preenchimento do formulário informando o usuário no caso de erros.
     * @return Verdadeiro se for válido.
     */
    boolean validateForm() {
        // Verifica a digitação do campo nome, se estiver vazio informa o usuário
        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameEditText.setError("Insira o nome da atividade.");
            nameEditText.requestFocus();
            return false;
        }

        // Verifica a digitação do campo de repetitições caso ele esteja selecionado.
        if (measureRepetitionsCheckbox.isChecked() && TextUtils.isEmpty(repetitionsEditText.getText())) {
            repetitionsEditText.setError("Insira a quantidade de repetições.");
            repetitionsEditText.requestFocus();
            return false;
        }

        // Verifica a digitação do campo de tempo limite caso ele esteja selecionado.
        if (measureTimeCheckbox.isChecked() && TextUtils.isEmpty(timeLimitEditText.getText())) {
            timeLimitEditText.setError("Insira o tempo limite.");
            timeLimitEditText.requestFocus();
            return false;
        }

        // Verifica se uma categoria foi selecionada
        if (categoryRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Selecione uma categoria!", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    //
    // -- TIPOS INTERNOS

    public static class TimePickerFragment
            extends DialogFragment {

        TimePickerDialog.OnTimeSetListener onTimeSetListener;

        public void setOnTimeSetListener(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
            this.onTimeSetListener = onTimeSetListener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new TimePickerDialog(getActivity(), onTimeSetListener, 8, 0, DateFormat.is24HourFormat(getActivity()));
        }

    }

}
