package com.misterveiga.cds.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class EmbedBuilds {

	public static EmbedBuilder getUserInfoEmbed(Member member, User user) {
		EmbedBuilder embed = new EmbedBuilder();
		String userAvatarUrl;
		if (member != null) {
			userAvatarUrl = member.getEffectiveAvatarUrl().toString();
		} else {
			userAvatarUrl = user.getAvatarUrl().toString();
		}
		embed.setAuthor(user.getName() + "#" + user.getDiscriminator(), userAvatarUrl, userAvatarUrl);

		if (member != null) {
			if (member.getNickname() != null) {
				embed.setDescription("This user is verified as: `" + member.getNickname() + "`");
			} else {
				embed.setDescription("This user is not verified");
				embed.setColor(0xFFFFFF);
			}

		} else {
			embed.setDescription("This user is not in this guild!");
			embed.setColor(0xFF0000);
		}

		if (member != null && member.getRoles() != null) {
			String allRoles = "";
			List<Role> roles = member.getRoles();
			for (Role item : roles) {
				allRoles += item.getAsMention().toString();
			}
			if (allRoles != "") {
				embed.addField("Roles", allRoles, true);
			}
		}

		OffsetDateTime timeStamp = user.getTimeCreated();
		long millisecondsSinceUnixEpoch = timeStamp.toInstant().toEpochMilli() / 1000;
		embed.addField("Created at",
				"<t:" + millisecondsSinceUnixEpoch + ":F>\n" + "(<t:" + millisecondsSinceUnixEpoch + ":R>)", true);

		if (member != null && member.getTimeJoined() != null) {
			OffsetDateTime timeStamp2 = member.getTimeJoined();
			long millisecondsSinceUnixEpoch2 = timeStamp2.toInstant().toEpochMilli() / 1000;
			embed.addField("Joined at",
					"<t:" + millisecondsSinceUnixEpoch2 + ":F>\n" + "(<t:" + millisecondsSinceUnixEpoch2 + ":R>)",
					true);
		}
		embed.setFooter("ID: " + user.getId());
		return embed;
	}
	
	public static EmbedBuilder alertMaintainerEmbed() {
		EmbedBuilder embed = new EmbedBuilder();

		embed.setColor(0x748bd8);
		embed.setTitle("Moderation Alerts");
		embed.setDescription(new StringBuilder() 
				    .append("There are pending moderation alerts in ")
				    .append(String.format("<#%d>", Properties.CHANNEL_MOD_ALERTS_ID))
				    .append("\n\n")
				    .append("Please remember to monitor moderation alerts frequently in order to avoid an accumulation of messages (and untreated reports) in the channel."));

		embed.setFooter("This message appears whenever there are alerts that are over 2 hours old.");

		return embed;
	}

	public static EmbedBuilder getRobloxUserInfoEmbed(String RobloxUserName, String UserId) {

		RobloxUserName = RobloxUserName.replaceAll("[^\\x00-\\x7F]", "");

		// erases all the ASCII control characters
		RobloxUserName = RobloxUserName.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

		// removes non-printable characters from Unicode
		RobloxUserName = RobloxUserName.replaceAll("\\p{C}", "");

		EmbedBuilder embed = new EmbedBuilder();

		try {
			OkHttpClient client = new OkHttpClient();

			Request request = new Request.Builder().url("https://users.roblox.com/v1/usernames/users").post(RequestBody
					.create(MediaType.parse("application/json"), "{\"usernames\": [ \"" + RobloxUserName + "\"]}"))
					.build();
			Response response = client.newCall(request).execute();
			ObjectNode obj = new ObjectMapper().readValue(response.body().string(), ObjectNode.class);

			String RobloxId = obj.get("data").get(0).get("id").toString();

			Request request2 = new Request.Builder().url("https://users.roblox.com/v1/users/" + RobloxId).build();
			Response response2 = client.newCall(request2).execute();
			ObjectNode obj2 = new ObjectMapper().readValue(response2.body().string(), ObjectNode.class);

			String displayName = obj2.get("displayName").toString().replace("\"", "");
			String DateString = obj2.get("created").toString().replace("\"", "");

			Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(DateString);
			long unixCreated = date.toInstant().toEpochMilli() / 1000;

			Request request3 = new Request.Builder()
					.url("https://thumbnails.roblox.com/v1/users/avatar-headshot?userIds= " + RobloxId
							+ "&size=720x720&format=Png&isCircular=false")
					.build();
			Response response3 = client.newCall(request3).execute();
			ObjectNode obj3 = new ObjectMapper().readValue(response3.body().string(), ObjectNode.class);

			String AvatarUrl = obj3.get("data").get(0).get("imageUrl").toString().replace("\"", "");

			Request request4 = new Request.Builder()
					.url("https://users.roblox.com/v1/users/" + RobloxId + "/username-history").build();
			Response response4 = client.newCall(request4).execute();
			JsonNode obj4 = new ObjectMapper().readValue(response4.body().string(), ObjectNode.class);
			JsonNode arrayNode = obj4.get("data");

			String previousUsernames = "";
			for (JsonNode jsonNode : arrayNode) {
				previousUsernames += "`" + jsonNode.get("name").asText() + "`\n";
			}

			embed.setAuthor(RobloxUserName + " (" + displayName + ")", AvatarUrl, AvatarUrl);
			if (previousUsernames != "") {
				embed.addField("Username History", previousUsernames, true);
			}
			embed.addField("Created", "<t:" + unixCreated + ":F> \n (<t:" + unixCreated + ":R>)", false);
			embed.addField("Roblox Profile", "https://roblox.com/users/" + RobloxId, false);
			if (UserId != null) {
				embed.setFooter("ID: " + UserId);
			}
			embed.setColor(0x2e3136);

			return embed;
		} catch (Exception e) {
			System.out.println(e);
			embed.setTitle("It appears the Roblox API is currently not responding! Please Try again later! :(");

			return embed;
		}
	}

	public static EmbedBuilder scanUrl(String url, String botIcon) throws InterruptedException {
		try {
		ObjectNode obj = VirusTotal.getScannedUrlData(url);
		String scanning = obj.get("verbose_msg").asText().toString().replace("\"", "");

		System.out.println(scanning);
		if (scanning.contains("queue")) {
			Thread.sleep(5000);
			obj = VirusTotal.getScannedUrlData(url);
		}

		EmbedBuilder embed = new EmbedBuilder();

		String retrievedUrl = obj.get("url").toString().replace("\"", "");
		int positives = obj.get("positives").asInt();

		embed.setAuthor("Malicious Log", null, botIcon);

		if (positives == 0) {
			embed.setTitle("No Malicious Content Detected");
			embed.setThumbnail("https://icons.iconarchive.com/icons/paomedia/small-n-flat/512/sign-check-icon.png");
			embed.addField("Link:", "`" + retrievedUrl + "`", false);
			embed.addField("State:", "`No Malicious content was detected`", false);
			embed.setColor(0x2ecc71);
		} else {
			embed.setTitle("Malicious Link Detected");
			embed.setThumbnail("https://icons.iconarchive.com/icons/paomedia/small-n-flat/1024/sign-error-icon.png");
			embed.setDescription("Do not click the link below as it has been flagged as malicious.");
			embed.addField("Link:", "`" + retrievedUrl + "`", false);
			embed.addField("State:", "`Malicious`", false);
			embed.setColor(0xe74c3c);

			String Flags = "";
			JsonNode arrayNode = obj.get("scans");

			final Iterator<Map.Entry<String, JsonNode>> fields = arrayNode.fields();
			while (fields.hasNext()) {
				final Map.Entry<String, JsonNode> field = fields.next();

				final String fieldName = field.getKey();
				final JsonNode value = field.getValue();
				final String result = value.get("result").toString().replace("\"", "");

				if (value.get("detected").asBoolean() == true) {
					Flags += "⊗ " + fieldName + "  : " + result + "\n";
				}
			}
			embed.addField("Flags:", "```" + Flags + "```", false);
		}

		embed.setFooter("Always be safe on the internet. Do not click any suspicious links!");

		return embed;
		} catch (Exception e) {
			return ApiError();
		}
	}

	public static EmbedBuilder ApiError() {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("something went wrong! Please try again later.");
		return embed;
	}
	
	public static EmbedBuilder getMutedDMEmbed(GuildMemberRoleAddEvent event) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("You have been muted. This means your chat permissions have been temporarily restricted.");
		embed.setThumbnail(event.getGuild().getIconUrl());
		embed.setDescription("Please use this time to read <#801557724503736335>. Once your mute expires, you will regain access to the chat and voice channels.\r\n"
				+ "\r\n"
				+ "This mute does not affect your account on Roblox.com.\r\n\n");
		embed.addField( "<:info:452813376788234250> **Mute Reason**\r\n",
				 "If you are unsure why you have been muted, or you want to know how long you are muted for then please DM a Moderator.\r\n", false);
		embed.addField( "<:z_qm60:452813334429827072> **Mute Bypassing**\r\n",
				 "Attempting to bypass a mute will result in you being banned from the server. <:ban_hammer:234839744092176384>", false);
		embed.setFooter("Roblox Unofficial Discord • " + LocalDate.now().getMonthValue() + "/" + LocalDate.now().getDayOfMonth() +"/" + LocalDate.now().getYear() , event.getGuild().getIconUrl());
		return embed;
	}
}
