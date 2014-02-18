package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class Team(id: Pk[Long] = NotAssigned, name: String, manager_id: String)

object Team {
    
  /**
   * Parse a Team from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("team.id") ~
    get[String]("team.name") ~
    get[String]("team.manager_id") map {
      case id~name~manager_id => Team(id, name, manager_id)
    }
  }
  
  /**
   * Construct the Map[String,String] needed to fill a select options set.
   */
  def options: Seq[(String,String)] = DB.withConnection { implicit connection =>
    SQL("select * from team order by name").as(Team.simple *).map(c => c.id.toString -> c.name)
  }
  
}