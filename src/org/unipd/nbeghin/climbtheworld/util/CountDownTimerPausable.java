package org.unipd.nbeghin.climbtheworld.util;

import android.os.CountDownTimer;

/**
 * Classe astratta che utilizza il nativo {@link CountDownTimer} per definire un timer che può essere 
 * messo in pausa e poi fatto ripartire da dove era arrivato in precedenza.
 * 
 */
public abstract class CountDownTimerPausable {
	
	//vari campi per creare il countdown timer
    long millisInFuture = 0;
    long countDownInterval = 0;
    long millisRemaining =  0;
    CountDownTimer countDownTimer = null;
    boolean isPaused = true;

    /**
     * Costruttore della classe che definisce il countdown timer.
     * @param millisInFuture tempo rimanente (in millisecondi)
     * @param countDownInterval intervallo di countdown (in millisecondi)
     */
    public CountDownTimerPausable(long millisInFuture, long countDownInterval) {
        super();
        this.millisInFuture = millisInFuture;
        this.countDownInterval = countDownInterval;
        this.millisRemaining = this.millisInFuture;
    }
    
    
    /**
     * Metodo che permette di creare il countdown timer chiamando il costruttore del nativo
     * {@link CountDownTimer} e implementando i suoi metodi {@code onTick(long)} e {@code onFinish()}.
     */
    private void createCountDownTimer(){
        countDownTimer = new CountDownTimer(millisRemaining,countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished;
                CountDownTimerPausable.this.onTick(millisUntilFinished);
            }

            
            @Override
            public void onFinish() {
                CountDownTimerPausable.this.onFinish();
            }
        };
    }
    
    
    /**
     * Metodo chiamato ad intervalli regolari.
     * 
     * @param millisUntilFinished tempo rimanente (in millisecondi) 
     */
    public abstract void onTick(long millisUntilFinished);
    
    
    /**
     * Metodo chiamato quando il tempo è scaduto.
     */
    public abstract void onFinish();
    
    
    /**
     * Metodo che cancella il countdown.
     */
    public final void cancel(){
    	if(countDownTimer!=null){
    		countDownTimer.cancel();
        }
        this.millisRemaining = 0;
    }
    
    
    /**
     * Metodo che avvia o riprende il conto alla rovescia.
     * @return CountDownTimerPausable l'istanza corrente del countdown timer
     */
    public synchronized final CountDownTimerPausable start(){
        if(isPaused){
            createCountDownTimer();
            countDownTimer.start();
            isPaused = false;
        }
        return this;
    }
    
    
    /**
     * Metodo che mette in pausa in countdown timer, in modo che dopo possa essere ripreso (avviato)
     * dallo stesso punto in cui era stato interrotto.
     */
    public void pause() throws IllegalStateException{
    	if(isPaused==false){
    		countDownTimer.cancel();
    	} 
    	else{
    		throw new IllegalStateException("CountDownTimerPausable is already in pause state, start counter before pausing it.");
        }
    	isPaused = true;
    }
    
    
    /**
     * Metodo che informa se il countdown timer è in pausa o meno.
     * @return 'true' se il countdown timer è in pausa, 'false' altrimenti
     */
    public boolean isPaused() {
        return isPaused;
    }
}