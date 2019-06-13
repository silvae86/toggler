package startup;

import com.google.inject.AbstractModule;

public class StartupBinder extends AbstractModule {

    protected void configure() {

        bind(Startup.class).asEagerSingleton();
    }

}