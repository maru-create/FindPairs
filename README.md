# FindPairsGame  
マインクラフトで遊べるゲームです。  
出現した生物やモンスターの中から***ペアを見つけて***高得点を目指すゲームです。  

# プレイ動画  


https://github.com/user-attachments/assets/2e1af3df-b738-4ec7-9d46-94663109c424


# ゲームの概要  
指定のコマンドを入力すると、エンティティ（生物 or モンスター）が10体出現します。  
エンティティはペアになっており、ペアを揃えることで難易度に応じた得点をゲットできます。  
制限時間内により多くの得点をゲットしましょう！  


|  難易度  |   エンティティ   |   備考  |
| :---:   |     :---:    |   :---: |
| easy    |    TURTLE    |         |
| normal  |      PIG     |         |
| hard    |    SKELETON  | 体力・空腹度MAX、防具を装備  |  

# 遊び方  
1. ゲーム開始コマンドを入力してください。  
**（3つの難易度から1つを選択）**  

難易度 easy：  
```  
/findpairs easy
```  

難易度 normal：  
```  
/findpairs normal  
```  

難易度 hard：  
```  
/findpairs hard  
```

2. 生物やモンスターが10体出現します。  
3. 右クリックしてエンティティを選択してください。  
4. 残りのエンティティの中から、ペアとなっているエンティティを選択してください。  
5. ペアの選択に”成功” → 得点ゲット！  
   ペアの選択に”失敗” → やり直し  
6. 制限時間30秒以内にたくさんのペアを揃えてください。  
# スコアの確認動画


https://github.com/user-attachments/assets/363ff9dc-cdd9-4bea-b816-94912c2ab4e3


今までのスコア一覧を表示するコマンド  

```  
/findpairs list  
```

# データベース  
データベース接続方法  
``` mysql  
CREATE DATABASE spigot_server;  
USE spigot_server;  
CREATE TABLE find_pairs_score (id int auto_increment, player_name varchar(100)
, score int, difficulty varchar(30),registered_at datetime, primary key(id));
```  

# 対応バージョン

・Minecraft:1.21.1  
・Spigot:1.21.1
