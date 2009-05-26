package com.danga.MemCached.Flare;

public interface ErrorHandler extends com.danga.MemCached.ErrorHandler {

    /**
     * Called for errors thrown during {@link FlareClient#ping()} and related methods.
     */
    public void handleErrorOnPing( final FlareClient client ,
                                   final Throwable error );

    /**
     * Called for errors thrown during {@link FlareClient#kill(String, long)} and related methods.
     */
    public void handleErrorOnKill( final FlareClient client ,
                                   final Throwable error );

    /**
     * Called for errors thrown during {@link FlaredClient#dump()} and related methods.
     */
    public void handleErrorOnDump( final FlaredClient client ,
                                   final Throwable error );

    /**
     * Called for errors thrown during {@link FlareiClient#shiftNodeRole(String, int, Role, int)} and related methods.
     */
    public void handleErrorOnNodeRole( final FlareiClient client ,
                                   final Throwable error );

    /**
     * Called for errors thrown during {@link FlareiClient#shiftNodeState(String, int, State)} and related methods.
     */
    public void handleErrorOnNodeState( final FlareiClient client ,
                                   final Throwable error );
    /**
     * Called for errors thrown during {@link com.danga.MemCached.Flare.FlareiClient#removeNode(String, int)} and related methods.
     */
    public void handleErrorOnRemoveNode( final FlareiClient client ,
                                   final Throwable error );
}
