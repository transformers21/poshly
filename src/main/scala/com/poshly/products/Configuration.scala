package com.poshly.products

import javax.mail.{Authenticator, PasswordAuthentication}

import com.poshly.accounts.client.{UserService, UserServiceCluster}
import com.poshly.core.configuration._
import com.poshly.core.mailer.Mailer.{From, MailTypes, Subject}
import com.poshly.core.mailer.{Mailer, MailerConfiguration, SMTPMailConfiguration, SMTPMailer}
import com.poshly.core.redis.{RedisComponent, RedisConfiguration}
import com.poshly.core.zk.ZooKeeperClientComponent
import com.poshly.geocode.client.{AddressService, AddressServiceCluster, AddressingService}
import com.poshly.sparkservice.client.{AnalyticsService, AnalyticsServiceCluster}
import com.poshly.pie.client.{AnswerService, AnswerServiceCluster, InsightDefinitionService, InsightDefinitionServiceCluster}
import com.poshly.qms.client._

trait BaseConfiguration extends Configuration {
  val clustered = config.getBoolean("finagle.cluster")
}

trait AnswersConfiguration extends BaseConfiguration {
  val answers: AnswerService
}

trait AnswersComponent extends AnswersConfiguration with QuestionServiceConfiguration {
  lazy val answers = {
    if (clustered) {
      AnswerServiceCluster(config)
    } else {
      AnswerService(config)
    }
  }
}

trait AnalyticsConfiguration extends BaseConfiguration {
  val analytics: AnalyticsService
}

trait AnalyticsComponent extends AnalyticsConfiguration {
  val analytics = {
    if (clustered) {
      AnalyticsServiceCluster(config)
    } else {
      AnalyticsService(config)
    }
  }
}

trait QuestionServiceConfiguration extends BaseConfiguration with RedisConfiguration {
  val questions: QuestionService
}

trait QuestionServiceComponent extends QuestionServiceConfiguration {
  val questions: QuestionService = {
    if (clustered) {
      QuestionServiceCluster(config)
    } else {
      QuestionService(config)
    }
  }
}

trait InsightDefinitionConfiguration extends BaseConfiguration {
  val insightDefinitions: InsightDefinitionService
}

trait InsightDefinitionComponent extends InsightDefinitionConfiguration {
  val insightDefinitions = {
    if (clustered) {
      InsightDefinitionServiceCluster(config)
    } else {
      InsightDefinitionService(config)
    }
  }
}

trait ProductConfiguration extends AccountUserConfiguration with RedisConfiguration with BaseConfiguration {
  val products: ProductService
}

trait ProductComponent extends ProductConfiguration {
  val products = {
    if (clustered) {
      ProductServiceCluster(config)
    } else {
      ProductService(config)
    }
  }
}

trait OfferConfiguration extends AccountUserConfiguration with RedisConfiguration with BaseConfiguration {
  val offerService: OfferService
}

trait OfferComponent extends OfferConfiguration {
  val offerService = {
    if (clustered) {
      OfferServiceCluster(config)
    } else {
      OfferService(config)
    }
  }
}

trait AccountUserConfiguration extends RedisConfiguration with BaseConfiguration {
  val userService: UserService
}

trait AccountUserComponent extends AccountUserConfiguration {
  val userService = {
    if (clustered) {
      UserServiceCluster(config)
    } else {
      UserService(config)
    }
  }
}

trait GeocodeConfiguration extends BaseConfiguration {
  val addressService: AddressService
}

trait GeocodeComponent extends GeocodeConfiguration {
  val addressService = {
    if (clustered) {
      AddressServiceCluster(config)
    } else {
      AddressingService(config)
    }
  }
}

trait MailerComponent extends MailerConfiguration with Configuration {
  val mailer: Mailer = {
    val disabled = config.getBoolean("products.notifications.disable")
    if (disabled) {
      new Mailer {
        private def logSend(from: From, subject: Subject, rest: Seq[MailTypes]) = {
          logger.info(s"logSend from: $from, subject: $subject")
          logger.debug(rest)
        }

        override def send(from: From, subject: Subject, rest: MailTypes*) = logSend(from, subject, rest)

        override def syncSend(from: From, subject: Subject, rest: MailTypes*) = logSend(from, subject, rest)
      }
    } else {
      val configuration = SMTPMailConfiguration.build(config)
      val username = config.getString("mail.username")
      val password = config.getString("mail.password")

      val authentication = new Authenticator() {
        override def getPasswordAuthentication = new PasswordAuthentication(username, password)
      }
      val mailer = new SMTPMailer(configuration, Some(authentication))
      mailer
    }
  }
}


class ProductsComponents extends
  AnswersComponent with
  AnalyticsComponent with
  QuestionServiceComponent with
  InsightDefinitionComponent with
  ProductComponent with
  OfferComponent with
  ZooKeeperClientComponent with
  AccountUserComponent with
  GeocodeComponent with
  RedisComponent with
  MailerComponent
