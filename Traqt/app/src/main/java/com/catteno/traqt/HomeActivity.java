package com.catteno.traqt;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.catteno.traqt.adapters.ActivitiesAdapter;
import com.catteno.traqt.helpers.IntentExtras;
import com.catteno.traqt.helpers.TypefaceSpan;
import com.catteno.traqt.model.DataStore;
import com.catteno.traqt.model.entities.TraqtActivity;

import java.util.List;

public class HomeActivity
        extends AppCompatActivity
        implements IntentExtras {

    //
    // -- CAMPOS

    DataStore dataStore;
    List<TraqtActivity> allActivities;
    ListView activitiesListView;
    TextView noActivitiesTextView;

    //
    // -- CICLO DE VIDA DA ATIVIDADE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Exibe o título usando a fonte personalizada
        SpannableString s = new SpannableString("TRAQT");
        s.setSpan(new TypefaceSpan(this, "Days.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, FormActivity.class);
                startActivityForResult(intent, REQUEST_FORM);
            }
        });

        // Inicializa o Singleton de armazenamento de dados
        DataStore.initialize(this);
        dataStore = DataStore.getInstance();

        activitiesListView = (ListView)findViewById(R.id.activities_list_view);
        activitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtém o item selecionado
                TraqtActivity act = allActivities.get(position);

                // Cria e configura o Intent, e inicia a nova atividade
                Intent intent = new Intent(HomeActivity.this, DetailsActivity.class);
                intent.putExtra(ACTIVITY_ID_EXTRA, act.getId());
                startActivityForResult(intent, REQUEST_DETAIL);
            }
        });
        noActivitiesTextView = (TextView)findViewById(R.id.no_activities_text_view);

        refreshListView(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // "Infla" o menu adicionando seus items ao ActionBar
        getMenuInflater().inflate(R.menu.menu_home, menu);

        // Obtemos o SearchView e configuramos suas propriedades
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                refreshListView(newText);
                return true;
            }
        });

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FORM) {
            // Atualiza a listagem caso uma nova atividade tenha sido inserida.
            if (resultCode == RESULT_INSERTED) {
                refreshListView(null);
            }
        }
        else if (requestCode == REQUEST_DETAIL) {
            // Atualiza a listagem de atividades caso os registros tenham sofrido algum tipo de alteração.
            if (resultCode == RESULT_UPDATED || resultCode == RESULT_DELETED)
                refreshListView(null);
        }
    }

    //
    // -- MÉTODOS PRIVADOS

    /**
     * Atualiza a lista de atividades no listView
     * @param searchTerm Termo de busca. Opcional, se passar nulo retorna todos os registros.
     */
    void refreshListView(String searchTerm) {
        // Configura um Adapter usando as atividades registradas no banco
        if (searchTerm == null) {
            allActivities = dataStore.getActivityRepository().findAll();
        } else {
            allActivities = dataStore.getActivityRepository().findWhere("name LIKE ?", searchTerm.concat("%"));
        }
        TraqtActivity[] activities = allActivities.toArray(new TraqtActivity[allActivities.size()]);
        ActivitiesAdapter activitiesAdapter = new ActivitiesAdapter(this, activities);

        // Associa o adapter ao ListView
        activitiesListView.setAdapter(activitiesAdapter);

        // Verifica se há registros
        if (allActivities.isEmpty())
            noActivitiesTextView.setVisibility(View.VISIBLE);
        else
            noActivitiesTextView.setVisibility(View.GONE);
    }
}
