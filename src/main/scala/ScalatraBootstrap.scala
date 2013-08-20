import com.typesafe.scalalogging.slf4j.Logging
import org.mbari.odss.services.timeline._

import com.mongodb.casbah.Imports._
import com.mongodb.ServerAddress
import com.typesafe.config.ConfigFactory
import java.util.ServiceConfigurationError
import org.mbari.odss.services.timeline.{OdssPlatformTimelineSwagger, TokensController}
import org.scalatra._
import javax.servlet.ServletContext
import java.io.File


/**
 *
 */
class ScalatraBootstrap extends LifeCycle with Logging {

  private[this] var setupOpt: Option[Setup] = None

  override def init(context: ServletContext) {

    logger.info(s"contextPath = '${context.getContextPath}'")

    val configFilename = context.getInitParameter("configFile")
    if (configFilename == null) {
      throw new ServiceConfigurationError("Could not retrieve configuration parameter: configFile.  Check web.xml")
    }

    val setup = new Setup(configFilename)
    import setup._

    context mount(new PlatformsController,   "/platforms/*")
    context mount(new TokensController,      "/tokens/*")
    context mount(new PeriodsController,     "/periods/*")
    context mount(new TimelinesController,   "/timelines/*")
    context mount(new ResourcesApp,          "/api-docs/*")

    setupOpt = Some(setup)
  }

  override def destroy(context: ServletContext) {
    setupOpt foreach { _.destroy() }
  }
}
