package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ModelSpec extends Specification {
  
  import models._

  // -- Date helpers
  
  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str
  
  // --
  
  "Device model" should {
    
    "be retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val Some(macintosh) = Device.findById(21)
      
        macintosh.name must equalTo("Macintosh")
        //macintosh.introduced must beSome.which(dateIs(_, "1984-01-24"))  
        
      }
    }
    
    "be listed along its companies" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val devices = Device.list()

        devices.total must equalTo(574)
        devices.items must have length(10)

      }
    }
    
    "be updated if needed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        Device.update(21, Device(name="The Macintosh", version=None, teamId=Some(1), borrowed_date=None, borrowed_user_id=None, status=true))
        
        val Some(macintosh) = Device.findById(21)
        
        macintosh.name must equalTo("The Macintosh")
        macintosh.borrowed_date must beNone
        
      }
    }
    
  }
  
}