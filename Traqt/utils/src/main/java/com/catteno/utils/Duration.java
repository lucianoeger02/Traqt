package com.catteno.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Formata e extrai os dados de uma duração em diversas unidades.
 */
public class Duration {

    //
    // -- CAMPOS

    /**
     * Segundos são a unidade base da duração, as demais unidades são obtidas através dessa.
     */
    long seconds;

    //
    // -- CONSTRUTORES

    /**
     * Cria uma instância dessa classe usando a quantidade de segundos como base.
     * @param seconds Quantidade de segundos da duração.
     */
    public Duration(long seconds) {
        this.seconds = seconds;
    }

    //
    // -- MÉTODOS PÚBLICOS

    /**
     * Obtém a quantidade de segundos dessa duração.
     * @return Quantidade de segundos da duração.
     */
    public long getSeconds() {
        return seconds;
    }

    /**
     * Atribui a quantidade de segundos dessa duração.
     * @param seconds Quantidade de segundos.
     */
    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    /**
     * Obtém a quantidade total de horas dessa duração.
     * @return Total de horas.
     */
    public int getHours() {
        return (int)Math.floor((seconds / 60.0) / 60.0);
    }

    /**
     * Obtém a quantidade toal de minutos dessa duração.
     * @return Total de minutos.
     */
    public int getMinutes() {
        return (int)Math.floor(seconds / 60.0);
    }

    /**
     * Representa a porção de horas desta duração, dentro do espaço de um dia.
     * @return A hora dessa duração, de 0 a 23.
     */
    public int getHourComponent() {
        return getHours() % 24;
    }

    /**
     * Representa a porção de minutos dessa duração dentro do espaço de uma hora.
     * @return A quantidade de minutos, de 0 a 59.
     */
    public int getMinuteComponent() {
        return getMinutes() - (getHours() * 60);
    }

    /**
     * Representa a porção de segundos dessa duração dentro do espaço de um minuto.
     * @return A quantidade de segundos, de 0 a 59.
     */
    public int getSecondComponent() {
        return (int)(seconds - (getMinutes() * 60));
    }

    /**
     * Obtém a representação formatada dessa duração.
     * @return Uma string representando a duração no formato hh:MM:SS.
     */
    public String getFormattedDuration() {
        String hourComponent = StringUtils.leftPad(String.valueOf(getHourComponent()), 2, '0');
        String minuteComponent = StringUtils.leftPad(String.valueOf(getMinuteComponent()), 2, '0');
        String secondComponent = StringUtils.leftPad(String.valueOf(getSecondComponent()), 2, '0');

        return hourComponent + ":" + minuteComponent + ":" + secondComponent;
    }

}