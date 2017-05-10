import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import simulator.Simulator;
import structs.CharacterData;
import structs.FrameData;
import structs.GameData;

import commandcenter.CommandCenter;

import enumerate.Action;

/**
 * MCTSで利用するNode
 *
 * @author Taichi Miyazaki
 */
public class Node {

  /** UCT실행 시간*/
  public static final int UCT_TIME = 165 * 100000;//0.0165second

  /** UCB1 상수 C값*/
  public static final double UCB_C = 9;

  /** 탐험 트리의 깊이 */
  public static final int UCT_TREE_DEPTH = 6;

  /** 노드를 생성하는 임계치 */
  public static final int UCT_CREATE_NODE_THRESHOULD = 10;

  /** 시뮬레이션 시간*/
  public static final int SIMULATION_TIME = 60;

  /** 난수에 사용 */
  private Random rnd;

  /** 부모 노드 */
  private Node parent;

  /** 아들노드 */
  private Node[] children;

  /** 깊이 */
  private int depth;

  /** 노드 탐색 횟수 */
  private int games;

  /** UCB1값 */
  private double ucb;

  /** 평가값 */
  private double score;

  /** 선택할수 있는 자기의 이전Action */
  private LinkedList<Action> myActions;//내꺼

  /** 상대가 선택 할수 있는 이전Action */
  private LinkedList<Action> oppActions;//상대꺼

  /** 시뮬레이터 */
  private Simulator simulator;

  /** 탐색할때 선택된 나의 Action */
  private LinkedList<Action> selectedMyActions;

  /** 시뮬레이션 전에 나의HP */
  
  private int myOriginalHp;//적의 기존 체력

  /** 시뮬레이션 전에 적의HP */
  private int oppOriginalHp;//적의 기존체력

  private FrameData frameData;
  private boolean playerNumber;
  private CommandCenter commandCenter;
  private GameData gameData;

  private boolean isCreateNode;

  Deque<Action> mAction;
  Deque<Action> oppAction;

  public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
      LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
      CommandCenter commandCenter, LinkedList<Action> selectedMyActions) {
    this(frameData, parent, myActions, oppActions, gameData, playerNumber, commandCenter);

    this.selectedMyActions = selectedMyActions;
  }

  public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
      LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
      CommandCenter commandCenter) {
    this.frameData = frameData;
    this.parent = parent;
    this.myActions = myActions;
    this.oppActions = oppActions;
    this.gameData = gameData;
    this.simulator = new Simulator(gameData);
    this.playerNumber = playerNumber;
    this.commandCenter = commandCenter;

    this.selectedMyActions = new LinkedList<Action>();

    this.rnd = new Random();
    this.mAction = new LinkedList<Action>();
    this.oppAction = new LinkedList<Action>();

    CharacterData myCharacter = playerNumber ? frameData.getP1() : frameData.getP2();
    CharacterData oppCharacter = playerNumber ? frameData.getP2() : frameData.getP1();
    myOriginalHp = myCharacter.getHp();
    oppOriginalHp = oppCharacter.getHp();

    if (this.parent != null) {
      this.depth = this.parent.depth + 1;//부모가 있으면 깊이 추가
    } else {
      this.depth = 0; //부모 없으면 깊이 0
    }
  }//노드 정보 가져오기

  /**
   * MCTS진행
   *
   * @return 탐색 횟수가 많은 최종노드 Action
   */
  public Action mcts() {
    // 시간이 되는한 UCT를 반복한다.
    long start = System.nanoTime();
    for (; System.nanoTime() - start <= UCT_TIME;) {////0.0165초
      uct();//uct 작동
    }

    return getBestVisitAction();
  }

  /**
   * 플레이 아웃(시뮬레이션)을 한다.
   *
   * @return 시뮬레이션 값
   */
  public double playout() {

    mAction.clear();
    oppAction.clear();

    for (int i = 0; i < selectedMyActions.size(); i++) {// 가능 액션 추가
      mAction.add(selectedMyActions.get(i));
    }

    for (int i = 0; i < 5 - selectedMyActions.size(); i++) {// 가능액션이 5개 이하일 경우 들어가 있는 액션을 랜덤하게 추가하여 5개를 만든다.
      mAction.add(myActions.get(rnd.nextInt(myActions.size())));
    }

    for (int i = 0; i < 5; i++) {// 상대방 가능 액션 5개를 랜덤하게 가져온다.
      oppAction.add(oppActions.get(rnd.nextInt(oppActions.size())));
    }

    FrameData nFrameData =
        simulator.simulate(frameData, playerNumber, mAction, oppAction, SIMULATION_TIME);//그러한 테이블을 가지고 시뮬레이션한다.

    return getScore(nFrameData);
  }

  /**
   * UCT실행 <br>
   *
   * @return 평가값
   */
  public double uct() {

    Node selectedNode = null;//선택노드가 초기화
    double bestUcb;//초기화

    bestUcb = -99999;

    for (Node child : this.children) {
      if (child.games == 0) {// 탐색횟수가 없으면
        child.ucb = 9999 + rnd.nextInt(50);// ucb 초기값에 10000~ 10050 값을 넣는다.
      } else {
        child.ucb = getUcb(child.score / child.games, games, child.games);//아들스코어/ 아들노드의 탐색횟수 , 전체탐색횟수 아들노드 탐색 횟수  
      }


      if (bestUcb < child.ucb) {//만약 아들 노드가 더 좋으면
        selectedNode = child;//노드 변경
        bestUcb = child.ucb;//ucb값 변경
      }

      
    }

    double score = 0;
    if (selectedNode.games == 0) {//선택 노드의 탐색이 0이면
      score = selectedNode.playout();//시뮬레이팅
    } else {
      if (selectedNode.children == null) {//선택된 노드의 아들노드가 없다면
        if (selectedNode.depth < UCT_TREE_DEPTH) {//덧붙여 UCT 깊이보다 현재 깊이가 적다면<-- 여기까지함
          if (UCT_CREATE_NODE_THRESHOULD <= selectedNode.games) {// 선택노드의 탐색 횟수가 쓰레쉬홀드보다 크거나 같다면
            selectedNode.createNode();//노드를 만든다.
            selectedNode.isCreateNode = true;//노드는 있음
            score = selectedNode.uct();//점수는 uct 값으로 한다.
          } else {
            score = selectedNode.playout();//쓰레쉬 홀드보다 작다면, 시뮬레이션 값을 넣는다.
          }
        } else {
          score = selectedNode.playout();//UCT 깊이가 더 깊거나 같으면 시뮬레이션 해서 값을 넣는다.
        }
      } else {
        if (selectedNode.depth < UCT_TREE_DEPTH) {//만약 아들노드가 있고 UCT 깊이보다 작다면
          score = selectedNode.uct();// UCT 값을 넣는다.
        } else {
          selectedNode.playout();//아니면 시뮬레이팅
        }
      }

    }

    selectedNode.games++;//노드의 탐색횟수증가
    selectedNode.score += score;//노드의 점수 입력

    if (depth == 0) {//깊이가 0 이면 서치 끝 1추가
      games++;
    }

    return score;//점수반환
  }

  /**
   * 노드생성
   */
  public void createNode() {

    this.children = new Node[myActions.size()];// 아들 노드를 현재 상태에서 할수 있는 액션 사이즈 만큼 만든다.

    for (int i = 0; i < children.length; i++) {//아들 크기 만큼 진행

      LinkedList<Action> my = new LinkedList<Action>();
      for (Action act : selectedMyActions) {//선택된 액션수만큼 돌리고
        my.add(act);// 액션을 추가한다.
      }

      my.add(myActions.get(i));

      
      children[i] =
          new Node(frameData, this, myActions, oppActions, gameData, playerNumber, commandCenter,
              my);//아들노드 만듬
    }
  }

  /**
   * 최다 방문 횟수를 가진 노드의 Actionを返す
   *
   * @return 최다 방문 횟수의 노드 Action
   */
  public Action getBestVisitAction() {

    int selected = -1;
    double bestGames = -9999;
    

    for (int i = 0; i < children.length; i++) {

      if (JayBot2016.DEBUG_MODE) {
        System.out.println("eavaluation:" + children[i].score / children[i].games + ",Try count:"
            + children[i].games + ",ucb:" + children[i].ucb + ",Action:" + myActions.get(i));
      }//디버그 

      if (bestGames < children[i].games) {
        bestGames = children[i].games;
        selected = i;
      }//게임횟수가 탐색횟수보다 작으면 수정 탐색횟수가 많은녀석을 채택해야 하니까
    }

    if (JayBot2016.DEBUG_MODE) {
      System.out.println(myActions.get(selected) + ",before Try count:" + games);
      System.out.println("");
    }

    return this.myActions.get(selected);
  }

  /**
   * 최다 스코어의 노드 Action을 되돌린다.
   *
   * 최다 스코어의 노드 Action
   */
  public Action getBestScoreAction() {

    int selected = -1;
    double bestScore = -9999;

    for (int i = 0; i < children.length; i++) {//아들노드의 갯수에 따라 

      System.out.println("Eavaluation:" + children[i].score / children[i].games + ",Try Count:"
          + children[i].games + ",ucb:" + children[i].ucb + ",Action:" + myActions.get(i));
//평가 -> 점수/탐색횟수 탐색횟수, 어떤액션을 하는지
      double meanScore = children[i].score / children[i].games;//평가점수 score/탐색횟수
      if (bestScore < meanScore) {//지금 값이 더 좋으면 수정
        bestScore = meanScore;// 삽입
        selected = i;//선택된 액션의 넘버
      }
    }

    System.out.println(myActions.get(selected) + ",Before Try count:" + games);
    System.out.println("");

    return this.myActions.get(selected);
  }

  /**
   * 평가값을 반환한다.
   *
   * @param fd 다양한 정보가 담긴 데이터 ex) HP
   * @return 평가값
   */
  public double getScore(FrameData fd) {// 리워드 점수 계산
	  
    return playerNumber ? Math.abs((fd.getP2().hp)/(fd.getP1().hp+0.1))*1000  : Math.abs((fd.getP1().hp)/(fd
            .getP2().hp+0.1))*1000;//rewards value
  }

  /**
   * 평가 값과 전 플레이 아웃 시행 횟수와 그 Action의 플레이 아웃 시행 횟수부터 UCB1값을 되돌린다.
   *
   * @param score 評価値   
   * @param n 이전에 들어있는 노드의 시뮬레이션 횟수
   * @param ni 해당 Action의 시뮬레이션 횟수
   * @return UCB1값
   */
  public double getUcb(double score, int n, int ni) {
    return score + UCB_C * Math.sqrt((2 * Math.log(n)) / ni);// UCB1계산  평가값 + UCB* 루트(2*log(이전 시뮬레이션횟수)/해당액션 시뮬레이션 횟수)
  }

  public void printNode(Node node) {
    System.out.println("Before Try Count:" + node.games);
    for (int i = 0; i < node.children.length; i++) {
      System.out.println(i + ",Try count:" + node.children[i].games + ",Depth:" + node.children[i].depth
          + ",score:" + node.children[i].score / node.children[i].games + ",ucb:"
          + node.children[i].ucb);
    }
    System.out.println("");
    for (int i = 0; i < node.children.length; i++) {
      if (node.children[i].isCreateNode) {
        printNode(node.children[i]);
      }
    }
  }
}
