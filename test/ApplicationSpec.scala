package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification {
  
  import models._

  // -- Date helpers
  
  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str
  
  // --
  
  "Application" should {
    
    "redirect to the device list on /" in {
      
      val result = controllers.Application.inde(FakeRequest())
      
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == "/devices")
      
    }
    
    "list devices on the the first page" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val result = controllers.Application.list(0, 2, "")(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain("574 devices found")
        
      }      
    }
    
    "filter device by name" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val result = controllers.Application.list(0, 2, "Apple")(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain("13 devices found")
        
      }      
    }
    
    "create new device" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val badResult = controllers.Application.save(FakeRequest())
        
        status(badResult) must equalTo(BAD_REQUEST)
        
        val badDateFormat = controllers.Application.save(
          FakeRequest().withFormUrlEncodedBody("name" -> "FooBar", "introduced" -> "badbadbad", "company" -> "1")
        )
        
        status(badDateFormat) must equalTo(BAD_REQUEST)
        contentAsString(badDateFormat) must contain("""<option value="1" selected>Apple Inc.</option>""")
        contentAsString(badDateFormat) must contain("""<input type="text" id="introduced" name="introduced" value="badbadbad" >""")
        contentAsString(badDateFormat) must contain("""<input type="text" id="name" name="name" value="FooBar" >""")
        
        val result = controllers.Application.save(
          FakeRequest().withFormUrlEncodedBody("name" -> "FooBar", "introduced" -> "2011-12-24", "company" -> "1")
        )
        
        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/devices")
        flash(result).get("success") must beSome.which(_ == "Device FooBar has been created")
        
        val list = controllers.Application.list(0, 2, "FooBar")(FakeRequest())

        status(list) must equalTo(OK)
        contentAsString(list) must contain("One device found")
        
      }
    }
    
  }
  
}