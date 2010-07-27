package com.springsource.greenhouse.invite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.oauth.consumer.token.OAuthConsumerToken;
import org.springframework.security.oauth.extras.OAuthAccessToken;
import org.springframework.social.twitter.TwitterOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.springsource.greenhouse.account.Account;

@Controller
@RequestMapping("/invite/twitter")
public class TwitterInviteController {
	
	private TwitterOperations twitterService;
	
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Inject
	public TwitterInviteController(TwitterOperations twitterService, JdbcTemplate jdbcTemplate) {
		this.twitterService = twitterService;		
		this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public void friendFinder(Account account, Model model) {
		if (account.getUsername() != null) {
			model.addAttribute("username", account.getUsername());
		}
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public String findFriends(@OAuthAccessToken("twitter") OAuthConsumerToken accessToken, @RequestParam String username, Model model) {
		List<String> twitterFriends = twitterService.getFriends(accessToken, username);
		model.addAttribute("friends", findGreenhouseTwitterFriends(twitterFriends));
		return "invite/twitterFriends";
	}
	
	private List<GreenhouseFriend> findGreenhouseTwitterFriends(List<String> twitterFriends) {
	    return jdbcTemplate.query("select username, firstName, lastName from Member where username in ( :names )",
	    		Collections.singletonMap("names", twitterFriends),
	    		new RowMapper<GreenhouseFriend>() {
					public GreenhouseFriend mapRow(ResultSet rs, int rowNum) throws SQLException {
						GreenhouseFriend friend = new GreenhouseFriend();
						friend.setUsername(rs.getString("username"));
						friend.setName(rs.getString("firstName") + " " + rs.getString("lastName"));
					    return friend;
					}
				});
    }
}