package fr.songbird.scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import fr.songbird.exception.TooBigStringException;
import fr.songbird.survivalDevKit.Meter;
import fr.songbird.survivalDevKit.listeners.MeterListener;


/**
 * <br>Lib utilisees:
 * <ul style="color:#0055AA">
 *     <li>Spigot api 1.8.8-R0.1</li>
 *     <li>TBM-3.6.1_1-BETA</li>
 *     <li>WGRegionEvents</li>
 * </ul>
 * @author songbird
 * @version 0.1.2_3-ALPHA
 * @since 0.0.1_0-ALPHA
 */
public class StatNation implements MeterListener
{


	private Scoreboard board;
	//private int hebdo_Points;
	private String nation_name1;
	private int nation1_pt;
	private String nation_name2;
	private int nation2_pt;
	private String dispoHour;
	//private String current_Position;
	private String quotVillage;
	private ArrayList<ArrayList<Score>> scores = new ArrayList<ArrayList<Score>>();
	private HashMap<String, Objective> objectives = new HashMap<String, Objective>();
	private final Meter meter = new Meter();
	private ArrayList<String> objectivesName = new ArrayList<String>();
	private static final long LIMIT_TICK = 1000000000L;
	
	
	/**
	 * HashMap chargee de contenir toutes les informations relatives aux villages, comme par exemple:<br>
	 * Leur etat (disponible, non disponible)<br>
	 * Leur nom<br>
	 * Le nom de leur proprietaire (Lutha, Gondar, neutre)
	 */
	private HashMap<String, HashMap<String, String>> villageProfile = new HashMap<String, HashMap<String, String>>(); 
	private String colorPrefix;
	
	
	public StatNation(){}
	
	
	//#------ INNER CLASS ------#//
	
	class ObjectivesGenerator
	{
		String objectiveName, criteria;
		int scoreNumber, indexScore, indexScoreArray;
		String[] scoreNames;
		
		
		public void prepareGeneration
		(
				final String objectiveName, 
				final String criteria,
				final int scoreNumber,
				final int indexScore,
				final int indexScoreArray,
				final String...scoreNames
		)
		{
			this.objectiveName = objectiveName;
			this.criteria = criteria;
			this.scoreNumber = scoreNumber;
			this.indexScore = indexScore;
			this.indexScoreArray = indexScoreArray;
			this.scoreNames = scoreNames;
		}
		

		
		/**
		 * Voici un exemple de generation de scoreboard:<br>
		 * <pre>
		 * 		try {
				generateObjective
				(
					firstObjective,
					ChatColor.BOLD+"DYNASTIUM",
					"dummy",
					19,
					0,
					0,
					DisplaySlot.SIDEBAR,
					"AQUACqt/j:"+ChatColor.RED+"YOLO",
					"",
					"YELLOWTimer:",
					"",
					"YELLOWPoints:",
					"AQUALUTHA",
					"GOLDGondar",
					"",
					"REDBoss:",
					"AQUALUTHA",
					"GOLDGONDAR",
					"",
					"YELLOWConquetes:",
					"GOLDTherim",
					"AQUADanok",
					"GRAYDrustan",
					"GRAYOlrik",
					"GOLDDolium",
					"AQUAWEEK"
					
					
					
					
				);
			} catch (ArrayIndexOutOfBoundsException | TooBigStringException aiobe_tbse) 
			{
				
				aiobe_tbse.printStackTrace();
		    }
		 * </pre>
		 * @param o L'objectif
		 * @param objectiveName Le nom de l'objectif
		 * @param criteria Le critere de l'objectif
		 * @param scoreNumber Le nombre total de scores figurants sur le scoreboard (y compris les scores utilisés pour servir d'espaces)
		 * @param indexScore A partir de quel index part le generateur <br><strong>Parametre inutile qui sera supprime a la prochaine mise a jour a mettre <mark>imperativement</mark> a 0</strong>
		 * @param indexScoreArray Index a partir duquel l'ArrayList va commencer. (Si vous generez votre premier scoreboard, il est obligatoire de passer se parametre a 0)
		 * @param scoreNames Parametre variable qui contiendra chaque nom des scores affiches dans le scoreboard
		 * @throws ArrayIndexOutOfBoundsException
		 * @throws TooBigStringException
		 */
		private void generateObjective() throws ArrayIndexOutOfBoundsException, TooBigStringException
		{
			
			
			objectives.put(objectiveName, board.registerNewObjective(objectiveName, criteria));
			Objective o = objectives.get(objectiveName);
			o.setDisplayName(objectiveName);
			
			if(indexScoreArray == scores.size())
			{
				scores.add(new ArrayList<Score>());
			}
			else
			{
				throw new ArrayIndexOutOfBoundsException();
			}
			
			for(String string : scoreNames)
			{
				ChatColor color = getAdequateColor(string);
				String sub = string.equals("") ? "" : string.substring(string.indexOf(colorPrefix)+(colorPrefix.length()));
				if( sub.length() < 16)
				{
					if(getAdequateColor(string) != null)
					{
						scores.get(indexScoreArray).add(o.getScore(color+string.substring(string.indexOf(colorPrefix)+colorPrefix.length())));
					}
					else
					{
						scores.get(indexScoreArray).add(o.getScore(string));
					}
					System.out.println(indexScoreArray);
					System.out.println(indexScore);
					scores.get(indexScoreArray).get(indexScore).setScore(scoreNumber);
					
					indexScore++;
					scoreNumber--;
				}
				else
				{
					new TooBigStringException("La chaine de caracteres ["+string+"] possede "+string.length()+"caracteres au lieu de 16.");
				}
				colorPrefix = "";

			}
			
		}
	}

	
	private void initializeObjectives(ObjectivesGenerator...generators)
	{
		try 
		{
			for(ObjectivesGenerator generator : generators)
			{
				generator.generateObjective();
			}
		} catch (ArrayIndexOutOfBoundsException | TooBigStringException e) {
			e.printStackTrace();
		}
		
		scoreboardRotation();
	}
	
	/**
	 * La methode getAdequateColor permet de parser et colorer facilement une chaine de caracteres, exemple:
	 * <code>
	 * <pre>
	 * 		String word = "AQUAphrase";
	 * 		ChatColor color = getAdequateColor(word); // == ChatColor.AQUA
	 *      //La chaine de caracteres sera ensuite parsee de facon 
	 *      //a ce qu'elle ne possede plus son prefixe et pour enfin retrouver sa taille reelle
	 * 
	 * </pre>
	 * <code>
	 * @param word
	 * @return
	 */
	private ChatColor getAdequateColor(String word)
	{
		String wordUC = word.toUpperCase();
		if(wordUC.contains("AQUA"))
		{
			colorPrefix = "AQUA";
			return ChatColor.AQUA;
		}
		else if(wordUC.contains("BLACK"))
		{
			colorPrefix = "BLACK";
			return ChatColor.BLACK;
		}
		else if(wordUC.contains("BLUE"))
		{
			colorPrefix = "BLUE";
			return ChatColor.BLUE;
		}
		else if(wordUC.contains("BOLD"))
		{
			colorPrefix = "BOLD";
			return ChatColor.BOLD;
		}
		else if(wordUC.contains("DARK_AQUA"))
		{
			colorPrefix = "DARK_AQUA";
			return ChatColor.DARK_AQUA;
		}
		else if(wordUC.contains("DARK_BLUE"))
		{
			colorPrefix = "DARK_BLUE";
			return ChatColor.DARK_BLUE;
		}
		else if(wordUC.contains("DARK_GRAY"))
		{
			colorPrefix = "DARK_GRAY";
			return ChatColor.DARK_GRAY;
		}
		else if(wordUC.contains("DARK_GREEN"))
		{
			colorPrefix = "DARK_GREEN";
			return ChatColor.DARK_GREEN;
		}
		else if(wordUC.contains("DARK_PURPLE"))
		{
			colorPrefix = "DARK_PURPLE";
			return ChatColor.DARK_PURPLE;
		}
		else if(wordUC.contains("DARK_RED"))
		{
			colorPrefix = "DARK_RED";
			return ChatColor.DARK_RED;
		}
		else if(wordUC.contains("GOLD"))
		{
			colorPrefix = "GOLD";
			return ChatColor.GOLD;
		}
		else if(wordUC.contains("GRAY"))
		{
			colorPrefix = "GRAY";
			return ChatColor.GRAY;

		}
		else if(wordUC.contains("GREEN"))
		{
			colorPrefix = "GREEN";
			return ChatColor.GREEN;
		}
		else if(wordUC.contains("ITALIC"))
		{
			colorPrefix = "ITALIC";
			return ChatColor.ITALIC;
		}
		else if(wordUC.contains("LIGHT_PURPLE"))
		{
			colorPrefix = "LIGHT_PURPLE";
			return ChatColor.LIGHT_PURPLE;
		}
		else if(wordUC.contains("MAGIC"))
		{
			colorPrefix = "MAGIC";
			return ChatColor.MAGIC;
		}
		else if(wordUC.contains("RED"))
		{
			colorPrefix = "RED";
			return ChatColor.RED;
		}
		else if(wordUC.contains("YELLOW"))
		{
			colorPrefix = "YELLOW";
			return ChatColor.YELLOW;
		}
		
		return null;
	}
	
	private void scoreboardRotation()
	{
		new java.lang.Thread(new Runnable()
				{
					@Override
					public void run()
					{
						meter.countTick(LIMIT_TICK);
					}
				}
				
			).start();
	}
	
	private void setObjectivesName(String objectiveName)
	{
		this.objectivesName.add(objectiveName);
	}
	
	public void setVillageProfile
	(
		String villageName,
		String ownerName,
		String state
    )
	{
		villageProfile.put(villageName, new HashMap<String, String>());
		HashMap<String, String> map = villageProfile.get(villageName);
		map.put("villagename", villageName);
		map.put("ownername", ownerName);
		map.put("state", state);
		map.put("dispotime", quotVillage.equals(villageName) ? getDispoHour() : "none");
	}
	
	
	public HashMap<String, String> getVillageProfile(String villageName)
	{
		HashMap<String, String> villageProfile = this.villageProfile.get(villageName);
		return villageProfile;
	}
	
	
	/**
	 * Initialise la position du joueur
	 * @param position Position du joueur
	 */
	public void setPosition(final String position)
	{
		//this.current_Position = position;
	}
	
	/**
	 * Initialise les points hebdomadaires
	 * @param points Les points hebdomadaires accumules par le joueur
	 */
	public void setHebdoPoint(int points)
	{
		//this.hebdo_Points = points;
	}

	
	public void setNationName1(String nation1)
	{
		this.nation_name1 = nation1;
	}
	
	public String getNationName1()
	{
		return nation_name1;
	}
	
	public void setNationName2(String nation2)
	{
		this.nation_name2 = nation2;
	}
	
	public String getNationName2()
	{
		return nation_name2;
	}
	
	
	/**
	 * 
	 * @param pt Points obtenus par la nation de Lutha
	 */
	public void setNation1Points(int pt)
	{
		this.nation1_pt = pt;
	}
	
	public int getNation1Points()
	{
		return nation1_pt;
	}

	/**
	 * Mutateur de Gondar 
	 * @param pt Points obtenus par la nation de Gondar
	 */
	public void setNation2Points(int pt)
	{
		this.nation2_pt = pt;
	}
	
	public int getNation2Points()
	{
		return nation2_pt;
	}
	
	/**
	 * Initialise le nombre d'heures restantes 
	 * @param hour Nombre d'heures pendant lesquelles le village est encore vulnerable
	 */
	public void setDispoHour(String hour)
	{
		dispoHour = hour;
	}
	
	public String getDispoHour()
	{
		return dispoHour;
	}
	
	public void setQuotVillage(String villageName)
	{
		quotVillage = villageName;
	}
	
	

	
	

	@Override
	public void whenTick(long tick) 
	{
		Random rand = new Random();
		if(tick == 5L)
		{

			Objective o = null;
			for(Entry<String, Objective> entry : objectives.entrySet())
			{
				setObjectivesName(entry.getKey());
			}
			
			o = objectives.get(objectivesName.get(rand.nextInt(objectivesName.size())));
			System.out.println(o.getName());
			System.out.println("Affichage de l'objectif.");
			o.setDisplaySlot(DisplaySlot.SIDEBAR);
			
		}
	}
	
	public void setCurrentScoreboard(Scoreboard board)
	{
		this.board = board;
	}
	
	public Scoreboard getCurrentScoreboard()
	{
		return board;
	}
	
	
	public void initializeScoreboard()
	{
		meter.addMeterListener(this);
		ObjectivesGenerator userProfile_conquest = new ObjectivesGenerator();
		userProfile_conquest.prepareGeneration
		(
				ChatColor.RED+"DYNASTIUM",
				"dummy",
				5,
				0,
				0,
				"BOLDCqt du jour:",
				"YELLOW[Nom village]",
				"",
				"BOLDYouni-coins:",
				"YELLOW[nombre points]"
		);
		ObjectivesGenerator nationProfile = new ObjectivesGenerator();
		nationProfile.prepareGeneration
		(		
				ChatColor.DARK_RED+"DYNASTIUM",
				"dummy",
				4,
				0,
				1,
				"BOLDPoints:",
				"YELLOWLutha:",
				"YELLOWGondar:",
				"GREENTimer:"
		);
		ObjectivesGenerator user_position = new ObjectivesGenerator();
		StringBuffer buffer = new StringBuffer();
		buffer.append(ChatColor.YELLOW);
		buffer.append(ChatColor.BOLD);
		buffer.append("Lieux:");
		user_position.prepareGeneration
		(				
				buffer.toString(),
				"dummy",
				2,
				0,
				2,
				"YELLOW[ile]",
				"YELLOW[position]"
		);
		initializeObjectives(userProfile_conquest, nationProfile, user_position);
	}


	@Override
	public void whenTick(int tick) 
	{
		
	}
	
	

}
