package com.catteno.traqt.model.repositories;

import android.database.Cursor;

import com.catteno.traqt.model.TraqtDbHelper;
import com.catteno.traqt.model.entities.TraqtSession;

/**
 * Repositório para entidade de Sessões.
 */
public class SessionRepository extends RepositoryBase<TraqtSession> {

    public SessionRepository(TraqtDbHelper dbHelper) {
        super(dbHelper, TraqtSession.getTableDefinition());
    }

    //
    // -- IMPLEMENTAÇÃO DOS MÉTODOS ABSTRATOS

    @Override
    protected TraqtSession fromCursor(Cursor cursor) {
        return  TraqtSession.fromCursor(cursor);
    }

}