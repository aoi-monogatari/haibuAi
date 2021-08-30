import java.util.ArrayList;
import java.util.Random;

import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import enumerate.Action;
import enumerate.State;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.Key;
import struct.MotionData;
import simulator.Simulator;


/**
 * Based off of Mutagen by Connor Gregorich-Trevor and JayBot GM by Man-Je Kim & Donghyeon Lee.
 * Inspired to simplify the decision making by ZoneAI by Frank Ying.
 * 
 */ 

public class HaibuAI implements AIInterface {
	
	private Simulator simulator;
	private Key key;
	private CommandCenter commandCenter;
	private boolean playerNumber;
	private GameData gameData;
	private FrameData frameData;
	private Random rnd;
	private FrameData simulatorAheadFrameData;
	private ArrayList<Action> myActions;
	private CharacterData myCharacter;
	@SuppressWarnings("unused")
	private CharacterData oppCharacter;
	private static final int FRAME_AHEAD = 14;
	  
	/** Stage Data */
	private static final int STAGELEFT = -240;
	private static final int STAGERIGHT = 680;
	private static final int CORNERLENIENCY = 200;

	/** Motion and Actions */
	private ArrayList<MotionData> myMotion;
	private Action[] actionAir;
	private Action[] actionMyAir;
	private Action[] actionGround;
	private Action[] actionGroundMeleeMid;
	private Action[] actionGroundFar;
	private Action[] actionGroundPersonal;
	private Action[] actionGroundNeutral;
	private Action[] myaction;
	private Action[] myaction_100;
	private Action[] myaction_50;
	private Action[] myaction_85;
	private Action spSkill;

	private CharacterName charName;
	   
	/** Spacing Constants */
	private int closeConst = 30;

	
	@Override
	public void getInformation(FrameData frameData, boolean playerNumber) {
		
		this.frameData = frameData;
		this.playerNumber = playerNumber;
		this.commandCenter.setFrameData(this.frameData, this.playerNumber);

		// Get character number and name
		myCharacter = frameData.getCharacter(playerNumber);
		oppCharacter = frameData.getCharacter(!playerNumber);

	}
	
	@Override
	public int initialize(GameData gameData, boolean playerNumber) {
		
		this.playerNumber = playerNumber;
	    this.gameData = gameData;

	    this.key = new Key();
	    this.frameData = new FrameData();
	    this.commandCenter = new CommandCenter();
	    this.rnd = new Random();

	    this.myActions = new ArrayList<Action>();
	      
	      String tmpcharname = this.gameData.getCharacterName(this.playerNumber);
	      if (tmpcharname.equals("ZEN"))
	         charName = CharacterName.ZEN;
	      else if (tmpcharname.equals("GARNET"))
	         charName = CharacterName.GARNET;
	      else if (tmpcharname.equals("LUD"))
	         charName = CharacterName.LUD;
	      else
	         charName = CharacterName.OTHER;
	      

	    simulator = gameData.getSimulator();

	    actionAir = new Action[] { Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
	            Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA, Action.AIR_D_DF_FB,
	            Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA, Action.AIR_D_DB_BB };
	    actionGround = new Action[] { Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
	            Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD, Action.CROUCH_GUARD, Action.THROW_A,
	            Action.THROW_B, Action.STAND_A, Action.STAND_B, Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA,
	            Action.STAND_FB, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB,
	            Action.STAND_F_D_DFA, Action.STAND_F_D_DFB, Action.STAND_D_DB_BB };
	    spSkill = Action.STAND_D_DF_FC;
	    
	    // Initializing Actions for each character specifically based on distance from the opponent
	    
		if (charName.name() == "ZEN") {
			closeConst = 30;
			actionMyAir = new Action[] { Action.AIR_GUARD, Action.AIR_B, Action.AIR_DA, Action.AIR_DB, Action.AIR_FA,
					Action.AIR_FB, Action.AIR_UB, Action.AIR_D_DF_FA, Action.AIR_D_DF_FB, Action.AIR_F_D_DFA,
					Action.AIR_F_D_DFB, Action.AIR_D_DB_BA, Action.AIR_D_DB_BB };
			actionGroundNeutral = new Action[] { Action.BACK_STEP, Action.JUMP, Action.FOR_JUMP, 
					Action.STAND_A, Action.CROUCH_FB, Action.STAND_D_DB_BB, Action.STAND_F_D_DFB,Action.STAND_D_DF_FA,
					Action.STAND_B, Action.CROUCH_B, Action.STAND_F_D_DFA, Action.STAND_D_DF_FC, Action.STAND_D_DF_FB};
			actionGroundPersonal = new Action[] { Action.STAND_D_DB_BA, Action.THROW_B, Action.STAND_A, Action.CROUCH_GUARD,
					Action.CROUCH_A, Action.FOR_JUMP, Action.JUMP, Action.CROUCH_FA, Action.CROUCH_FB,Action.THROW_A,
					Action.CROUCH_B, Action.STAND_B, Action.STAND_F_D_DFB, Action.STAND_D_DB_BB, Action.STAND_GUARD, Action.STAND_FA, Action.STAND_FB};
			actionGroundFar = new Action[] { Action.FORWARD_WALK, Action.DASH, Action.CROUCH_GUARD, Action.STAND_GUARD,
					Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.CROUCH_FB};
			actionGroundMeleeMid = new Action[] { Action.STAND_GUARD, Action.CROUCH_GUARD, Action.BACK_JUMP,
					Action.STAND_A, Action.STAND_B, Action.CROUCH_A, Action.CROUCH_B,Action.FOR_JUMP, Action.STAND_D_DF_FC,
					Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FB, Action.STAND_F_D_DFA,Action.STAND_F_D_DFB };
						
			} else if (charName.name() == "LUD") {				
				closeConst = 30;
				actionGroundMeleeMid = actionGround;
				actionGroundFar = new Action[] { Action.FORWARD_WALK, Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.CROUCH_FB};
				actionGroundNeutral = actionGround;
				actionMyAir = actionAir;
				actionGroundPersonal = new Action[] { Action.STAND_D_DB_BA, Action.STAND_GUARD, Action.FOR_JUMP,
						Action.CROUCH_GUARD, Action.THROW_A, Action.THROW_B, Action.STAND_A, Action.STAND_B,
						Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
						Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
						Action.STAND_F_D_DFB, Action.STAND_D_DB_BB, Action.STAND_D_DF_FC};
				

			} else if (charName.name() == "GARNET") {
				closeConst = 30;
				actionGroundPersonal = actionGround;
				actionGroundMeleeMid = actionGround;
				actionGroundNeutral = new Action[] { Action.DASH, Action.THROW_B, Action.THROW_A, Action.CROUCH_B, Action.STAND_D_DB_BB,
						Action.CROUCH_GUARD, Action.STAND_GUARD, Action.STAND_A, Action.STAND_FA, Action.FOR_JUMP,Action.STAND_F_D_DFB,
						Action.STAND_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.CROUCH_FA, Action.CROUCH_FB};
				actionGroundFar = new Action[] { Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.STAND_D_DF_FA};
				actionMyAir = new Action[] { Action.AIR_GUARD, Action.AIR_UA, Action.AIR_D_DF_FA, Action.AIR_B, Action.AIR_DB, 
						Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BB, Action.AIR_D_DB_BA, Action.AIR_FA};
				
				myaction= new Action[] {
						Action.FOR_JUMP, Action.STAND_D_DB_BB, Action.STAND_D_DF_FC, Action.STAND_GUARD, Action.CROUCH_GUARD, Action.STAND_D_DF_FB};
				myaction_50= new Action[] {
						Action.FOR_JUMP, Action.JUMP, Action.STAND_A,Action.STAND_B, Action.STAND_FA, Action.STAND_FB, Action.THROW_A, Action.THROW_B, 
						Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FB, Action.CROUCH_A, Action.CROUCH_B, Action.STAND_D_DF_FA, 
						Action.STAND_F_D_DFB, Action.STAND_D_DB_BB};
				myaction_85= new Action[] {
						Action.THROW_A, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_A, Action.CROUCH_B,Action.STAND_D_DB_BB,
						Action.STAND_D_DF_FA, Action.THROW_B, Action.STAND_A, Action.STAND_D_DF_FC, Action.STAND_F_D_DFB, Action.STAND_D_DB_BA};
				myaction_100= new Action[] {
						Action.CROUCH_FB, Action.CROUCH_A, Action.STAND_FB, Action.STAND_D_DB_BB, Action.AIR_FA,Action.STAND_F_D_DFB,
						Action.STAND_D_DB_BA, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_D_DF_FC, Action.THROW_B};

			}

	      		myMotion = gameData.getMotionData(this.playerNumber);
	      
	      		return 0;

	}

	@Override
	public void processing() {
		
		if (!frameData.getEmptyFlag() && frameData.getRemainingFramesNumber() > 0) {
			if (commandCenter.getSkillFlag())
				key = commandCenter.getSkillKey();
			else {
				key.empty();
				commandCenter.skillCancel();
				
				/**Obtaining FrameData with FRAME_AHEAD frames ahead
				 */
					simulatorAheadFrameData = simulator.simulate(frameData, playerNumber, null, null, FRAME_AHEAD);
					commandCenter.setFrameData(simulatorAheadFrameData, playerNumber);

					myCharacter = simulatorAheadFrameData.getCharacter(playerNumber);
					oppCharacter = simulatorAheadFrameData.getCharacter(!playerNumber);
					
					MyAttack();	
			}
		}
		
	}
	
	@Override
	public Key input() {
		
		return key;
	}		

	public void MyAttack() {
		myActions.clear();

		int energy = myCharacter.getEnergy();
		
		if (myCharacter.getState() == State.AIR) {
			for (int i = 0; i < actionMyAir.length; i++) {
				if (Math.abs(myMotion.get(Action.valueOf(actionMyAir[i].name()).ordinal())
						.getAttackStartAddEnergy()) <= energy) {
					myActions.add(actionMyAir[i]);
					myActions.add(Action.CROUCH_GUARD);
	                myActions.add(Action.STAND_GUARD);
				}
			}
			commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
		
		} else {
			// Each of these states is used for a different distance.
			if (Math.abs(myMotion.get(Action.valueOf(spSkill.name()).ordinal()).getAttackStartAddEnergy()) <= energy) {
				myActions.add(spSkill);
				commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
			}
			
			if ((charName.name() == "GARNET") && simulatorAheadFrameData.getDistanceX() <= 200) {
	             if(simulatorAheadFrameData.getDistanceX()<=100)
	             {
	                myActions.add(Action.CROUCH_GUARD);
	                myActions.add(Action.STAND_GUARD);
	                
	                for (int i = 0; i < myaction_50.length; i++) {
	                   if (Math.abs(myMotion.get(Action.valueOf(myaction_50[i].name()).ordinal())
	                         .getAttackStartAddEnergy()) <= energy) {
	                      myActions.add(myaction_50[i]);
	                      
	                   }
	                }
	            
	             commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
	             
	        } else if(simulatorAheadFrameData.getDistanceX()<=120)
	             {
	             	myActions.add(Action.CROUCH_GUARD);
	                myActions.add(Action.STAND_GUARD);
	             
	                for (int i = 0; i < myaction_85.length; i++) {
	                   if (Math.abs(myMotion.get(Action.valueOf(myaction_85[i].name()).ordinal())
	                         .getAttackStartAddEnergy()) <= energy) {
	                      myActions.add(myaction_85[i]);
	                      commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
	                   }
	                }
	                
	             commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
	                
	        } else if(simulatorAheadFrameData.getDistanceX()<=200) {
	             	myActions.add(Action.CROUCH_GUARD);
	                myActions.add(Action.STAND_GUARD);
	                
	                for (int i = 0; i < myaction_100.length; i++) {
	                   if (Math.abs(myMotion.get(Action.valueOf(myaction_100[i].name()).ordinal())
	                         .getAttackStartAddEnergy()) <= energy) {
	                      myActions.add(myaction_100[i]);
	                   }
	                }
	                
	             commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
	             
	        } else {
	                for (int i = 0; i < myaction.length; i++) {
	                   if (Math.abs(myMotion.get(Action.valueOf(myaction[i].name()).ordinal())
	                         .getAttackStartAddEnergy()) <= energy) {
	                      myActions.add(myaction[i]);
	                   }
	                }
	                
	             commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
	             
	        	}
	          }
			
			if (simulatorAheadFrameData.getDistanceX() < closeConst) {
				for (int i = 0; i < actionGroundPersonal.length; i++) {
					// Stops LUD from using moves with extremely long lag when
					// very near the opponent
					 if (Math.abs(myMotion.get(Action.valueOf(actionGroundPersonal[i].name()).ordinal())
							.getAttackStartAddEnergy()) <= energy) {
						int startup = myMotion.get(Action.valueOf(actionGroundPersonal[i].name()).ordinal())
								.getAttackStartUp();
						int frames = myMotion.get(Action.valueOf(actionGroundPersonal[i].name()).ordinal()).frameNumber;
						int endlag = frames - startup;
						if ((charName.name() == "LUD") && endlag < 45) {
							myActions.add(actionGroundPersonal[i]);
						} else if (charName.name() == "ZEN") {
							myActions.add(actionGroundPersonal[i]);
						}
					} 
				}
				
				commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
				
			if (myActions.size() == 0) {
					for (int i = 0; i < actionGroundPersonal.length; i++) {
						if (Math.abs(myMotion.get(Action.valueOf(actionGroundPersonal[i].name()).ordinal())
								.getAttackStartAddEnergy()) <= energy) {
							myActions.add(actionGroundPersonal[i]);
						}
						
					}
					
				commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
				
			}
			
			} else if (simulatorAheadFrameData.getDistanceX() <= 95) {
				for (int i = 0; i < actionGroundMeleeMid.length; i++) {
					if (Math.abs(myMotion.get(Action.valueOf(actionGroundMeleeMid[i].name()).ordinal())
							.getAttackStartAddEnergy()) <= energy) {
						myActions.add(actionGroundMeleeMid[i]);
						
					}
				}
				
				commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());

			} else if (simulatorAheadFrameData.getDistanceX() <= 500) {
				for (int i = 0; i < actionGroundNeutral.length; i++) {
					if (Math.abs(myMotion.get(Action.valueOf(actionGroundNeutral[i].name()).ordinal())
							.getAttackStartAddEnergy()) <= energy) {
						myActions.add(actionGroundNeutral[i]);
						
					}
				}
				
				commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
				
			} else if (simulatorAheadFrameData.getDistanceX() > 500) {
				for (int i = 0; i < actionGroundFar.length; i++) {
					if (Math.abs(myMotion.get(Action.valueOf(actionGroundFar[i].name()).ordinal())
							.getAttackStartAddEnergy()) <= energy) {
						myActions.add(actionGroundFar[i]);
						
					}
				}
				
				commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
				
			} else {
				for (int i = 0; i < actionGround.length; i++) {
					if (Math.abs(myMotion.get(Action.valueOf(actionGround[i].name()).ordinal())
							.getAttackStartAddEnergy()) <= energy) {
						myActions.add(actionGround[i]);
						
					}
				}
				
				commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
			}
		}
		
		// Some specific rules

		// Prevents characters from moving backwards when cornered, or LUD from moving backwards at all
		
		if (myCharacter.getLeft() < STAGELEFT + CORNERLENIENCY || myCharacter.getRight() > STAGERIGHT - CORNERLENIENCY
				|| (charName.name() == "LUD"))  {
			myActions.remove(Action.BACK_STEP);
			myActions.remove(Action.BACK_JUMP);
			commandCenter.commandCall((myActions.get(rnd.nextInt(myActions.size()))).name());
		}
		
}
	    
	
	@Override
	public void roundEnd(int p1Hp, int p2Hp, int frames) {
		 commandCenter.skillCancel();
	     key.empty();
	
	}
		
	
	public enum CharacterName {
         ZEN,GARNET,LUD,OTHER;
	}


	@Override
	public void close() {
		
	}


	  

}
