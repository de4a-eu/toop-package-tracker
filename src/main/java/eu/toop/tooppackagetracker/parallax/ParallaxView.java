package eu.toop.tooppackagetracker.parallax;

import com.vaadin.navigator.View;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import eu.toop.tooppackagetracker.Receiver;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@com.vaadin.annotations.JavaScript({
  "vaadin://jquery/jquery-3.3.1.js",
  "vaadin://js/package-tracker.js",
})

public class ParallaxView extends VerticalLayout implements View, Receiver.Listener {

  private UI _ui;
  JavaScript _javaScript;

  final VerticalLayout mainLayout = new VerticalLayout();
  final ParallaxLayout parallaxLayout = new ParallaxLayout();

  public ParallaxView (UI ui, JavaScript javascript) {
    _ui = ui;
    _javaScript = javascript;

    setWidth ("100000px");
    setHeight ("100%");

    mainLayout.setHeight("100%");
    mainLayout.setWidth("100000px");
    mainLayout.setStyleName("mainLayout");
    addComponent (mainLayout);
    mainLayout.addComponent(parallaxLayout);
  }

  @Override
  public void receive (ConsumerRecord<?, ?> consumerRecord) {
    String message = consumerRecord.value().toString();
    parallaxLayout.newSlice(message);
    _javaScript.execute("newSlice()");
    _ui.access(new Runnable() {
      @Override
      public void run() {
        _ui.getCurrent ().push();
      }
    });
  }
}
