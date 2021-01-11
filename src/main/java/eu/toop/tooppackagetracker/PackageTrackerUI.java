/**
 * Copyright (C) 2018-2021 toop.eu. All rights reserved.
 *
 * This project is dual licensed under Apache License, Version 2.0
 * and the EUPL 1.2.
 *
 *  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *         https://joinup.ec.europa.eu/software/page/eupl
 */
package eu.toop.tooppackagetracker;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.UI;

import eu.toop.tooppackagetracker.detail.DetailView;
import eu.toop.tooppackagetracker.parallax.ParallaxView;

@Theme ("PackageTrackerUITheme")
@SpringUI
@Push
// @PreserveOnRefresh
public class PackageTrackerUI extends UI implements IReceiverListener
{
  private Navigator navigator;

  @Override
  public void receive (final ConsumerRecord <?, ?> consumerRecord)
  {}

  @Override
  protected void init (final VaadinRequest request)
  {
    final ParallaxView parallaxView = new ParallaxView (this, JavaScript.getCurrent ());
    final DetailView detailView = new DetailView (this, JavaScript.getCurrent ());

    navigator = new Navigator (this, this);
    navigator.addView ("", parallaxView);
    navigator.addView (ParallaxView.class.getName (), parallaxView);
    navigator.addView ("detail", detailView);
  }

  @Override
  public void attach ()
  {
    super.attach ();
  }

  @Override
  public void detach ()
  {
    super.detach ();
  }
}
