package config

import org.scalatest.{Matchers, WordSpecLike}

class PublishConfigTest extends WordSpecLike with Matchers {
  "fromHostname" should {
    "correctly decode hostname" in {
      val hostname="beis-spp-dev"
      PublishConfig.fromHostname(hostname).publishUrl shouldBe "https://beis-ppr-dev/report-payment-practices"
    }
  }
}
