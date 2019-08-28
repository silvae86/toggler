package messaging.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ServicesWebSocketActor extends AbstractActor {
    private final ActorRef out;

    public ServicesWebSocketActor(ActorRef out) {
        this.out = out;
    }

    public static Props props(ActorRef out) {
        return Props.create(ServicesWebSocketActor.class, out);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> out.tell("I received your message: " + message, self()))
                .build();
    }
}
