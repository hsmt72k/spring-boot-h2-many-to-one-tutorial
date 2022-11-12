# Spring Boot でリレーションを持った DB を扱う REST API の作成を体験するチュートリアル

## Spring Data JPA の OneToMany を学ぼう

今日は、DB のリレーションに基づいて REST API を構築していく
ステップバイステップのチュートリアルを用意しました。

なかなか独学では難しい O/R マッパー、Hibernate、シリアル化といった仕組みやトピックが、手を動かして、挙動を見ていくことで理解できる内容です。

インストール不要の H2 DB を使うため、環境構築の必要もなく、2時間ぐらいで体験できます。

これで特訓を積めば、リレーションを持ったデータを登録・更新・削除する一連の API を1時間ぐらい（目指せ30分！）でコーディングできるようになると思います。

## モデルが1つの API から、モデルが複数でリレーションを持った API へ

このチュートリアルをやる前に、単純なモデル1つだけの REST API の作り方が分からない人は、
まずは30分でできるこちらのチュートリアルをやってほしい。

Spring Boot で軽量 DB を使った REST API の作成を体験するチュートリアル  
[https://github.com/hsmt72k/spring-boot-h2-rest-api-tutorial](https://github.com/hsmt72k/spring-boot-h2-rest-api-tutorial)

最初の REST API チュートリアルでは、モデルは1つだけ。Vegitable だけだった。

つまり、リレーション、紐づきがない。

賃貸マンション検索サイト、ブログ、勤怠管理アプリ、
そういったサイトやアプリに登録されるデータの大部分は、データどうしがリレーションでつながっている。

そこで、最初の REST API では1つしかなかったモデルにリレーションをはって、
他のモデルをつなぐ方法をここでは学んでいきたいと思う。

## O/R マッパー

Spring Boot には、Spring Data JPA という O/R マッパーがある。

O/R マッパーは、Object Relational Mapper の略で、
Java のオブジェクトと DB のテーブルをつなぐ仕組み（マッピング）である。

実際には、JPA の実装である、Hibernate というライブラリによって、
このJava のオブジェクトとテーブルを紐づける処理が行われる。

DB にアクセスをして、テーブルからデータを取得し、
そのデータを Java オブジェクトに詰め込むという作業は、
繰り返しと煩雑さがともなう作業だ。

定型的な SQL文を書き、実行したテーブルの列ごとに、
結果をオブジェクトに設定していくという単純作業が発生する。

O/R マッパーは、この苦労の多い作業を肩代わりしてくれる。

すでに、最初の REST API チュートリアルで、
O/R マッパーである JPA を、プロジェクトを作る際にパッケージとして追加した。

それでは、この JPA を使って、Vegitable モデル（VEGITABLES テーブル）に
他のテーブルを関連付ける作業を行っていこう。

## Color モデルの作成

色を表すモデルとして、Color モデルを新たに定義していく。

`model/Color.java`
``` java
@Entity
@Data
@Table(name = "COLORS")
public class Color {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "NAME_JA", nullable = false, unique=true)
	private String nameJa;

	@Column(name = "NAME_EN", nullable = false, unique=true)
	private String nameEn;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

	@OneToMany(
		fetch = FetchType.LAZY,
		mappedBy="color",
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	private List<Vegitable> vegitableList;
}
```

ここで特筆すべきは、「@OneToMany」というアノテーション。

このアノテーションにより、1対多で Vegitable モデルに紐づけようとしている。

Color モデルが「1」であり、Vegitable モデルが「多」である。

例えば、「赤」という1つの色に対して、
「トマト」「赤パプリカ」「ラディッシュ」など、複数の野菜があり得る。

1対多だが、厳密な関係でいけば、色と野菜の場合は、「1対0以上多」になる。

1つの色に対して、紐づく野菜は、0個の場合もあり得るし、1個の場合もあり得るし、
それ以上、複数の場合もあり得る。

COLORS テーブルに「赤」が登録されているが、
まだ VEGITABLES テーブルには赤色の野菜が1つも登録されていない状態が、1対0。

そして、赤色の野菜が1つ、2つとテーブルに登録されていき、1対多となる。

つまり、COLORS テーブルと VEGITABLES テーブルは、1対多の関係だ。

そのため、 @OneToMany アノテーションは、
Color モデルのフィールド、`List<Vegitable> vegitableList`
（Vegitable モデルのリスト）。

つまり Colors モデルは、「多」を持っている（紐づいている）ことになる。

そして、この @OneToMany アノテーションの mappedBy プロパティには、
紐づけ先の Vegitable モデルのフィールド名、color を指定する。

この指定により、親「Color」モデル、子「Vegitable」モデルが成立する。

次に、子側の Vegitable モデルにマッピング設定をしていく。

## Vegitable モデルへのマッピング

`model/Vegitable.java`
``` java
@Entity
@Data
@Table(name = "VEGITABLES")
public class Vegitable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "NAME", nullable = false, unique=true)
	private String name;

    // このフィールドは削除する
	// @Column(name = "COLOR_ID")
	// private String color;

	@Column(name = "PRICE")
	private int price;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    // このフィールドを追加する
	@ManyToOne
	@JoinColumn(name = "COLOR_ID")
    private Color color;
}
```

変更点は、元々あったフィールド、String color を削除すること。

追加点は、新規に Color color のフィールドを追加し、@ManyToOne アノテーションを付けること。
そしてさらに、@JoinColumn アノテーションを付けて、

こうすることで以下のような、テーブルがモデルに紐づけられることになる。

`VEGITABLES テーブル`
|ID |NAME |COLOR_ID |PRICE |CREATED_AT |UPDATED_AT |
|:-- |:-- |:-- |:-- |:-- |:-- |
|ID |名前 |COLOR_ID |価格 |登録日 |更新日 |

`COLORS テーブル`  
|ID |NAME_JA |NAME_EN |CREATED_AT |UPDATED_AT |
|:-- |:-- |:-- |:-- |:-- |
|ID |名前(日本語) |名前(英語) |登録日 |更新日 |


@JoinColumn アノテーションによって、VEGITABLES テーブルの COLOR_ID に FK（外部キー: foreign key）
が張られることになる。

Colors モデルの `List<Vegitable> vegitableList` につけられた、
`@OneToMany mappedBy = "color"` によって、COLOR_ID の参照先は、
COLORS テーブルの ID 列となる。

## Color リポジトリの作成

続いて、DB の Color テーブルにアクセスする ColorRepository インタフェースを新規作成する。

手始めに動作確認で使いたいメソッドは、継承元の JpaRepository の  getById(id) と save(model) だけなので、
インタフェースに実装は書かない。

`repository/ColorRepository.java`
``` java
public interface ColorRepository extends JpaRepository<Color, Long> {
}
```

## ColorService の作成

ColorRepository を使って、モデルを登録、id で取得するメソッドだけを実装した、
ColorService を作成する。

`service/ColorService.java`
``` java
@Service
public class ColorService {

    @Autowired
    ColorRepository colorRepository;

    public Color getById(long id) {
        Optional<Color> colorData = this.colorRepository.findById(id);

        if (colorData.isPresent())
            return colorData.get();
        return null;
    }

    public Color create(Color color) {
        LocalDateTime now = LocalDateTime.now();
        color.setCreatedAt(now);
        color.setUpdatedAt(now);
        return this.colorRepository.save(color);
    }
}
```

## ColorController の作成

そして、Color の取得・登録 API の問い合わせ窓口となる ColorController も新規作成する。

`controller/ColorController.java`
``` java
@RestController
@RequestMapping("/color")
public class ColorController {

    @Autowired
    ColorService colorService;

    @GetMapping("/{id}")
    public ResponseEntity<Color> getById(@PathVariable("id") long id) {
        Color color = this.colorService.getById(id);

        if (color != null)
            return new ResponseEntity<>(color, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/")
    public ResponseEntity<Color> createVegitable(@RequestBody Color color) {
        try {
            Color resultColor = this.colorService.create(color);
            return new ResponseEntity<>(resultColor, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
```

## color-api.http ファイルの作成

こちらも id での Color の取得、Color の登録をする API を記載しておく。

`rest-client\color-api.http`
``` md
### getById(id)
GET http://localhost:8080/color/1

### create(color)
POST http://localhost:8080/color/
content-type: application/json

{
  "nameJa": "赤",
  "nameEn": "red"
}
```

## プロジェクトの起動

プロジェクトを起動する前に application.yml で、Hibernate の設定を変更しておく。

変更箇所は、spring.jpa.hibernate.ddl-auto の値。

この ddl-auto の値を update から、create-drop に変更しておく。

``` yaml
spring:
  datasource:
    url: jdbc:h2:file:./h2db/vegitable
    driverClassName: org.h2.Driver
    username: sa
    password: pass
    
  jpa:
    show-sql: true
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      # ddl-auto 設定を update から create-drop に変更しておく
      # ddl-auto: update
      ddl-auto: create-drop
```

この ddl-auto は、何のための設定かというと、
プロジェクトを起動したときに、DDL（Data Definition Language: データ定義言語）をどう発行するかを決めるための設定である。

DDL とは、テーブルやカラムの新規作成・変更・削除を行う SQL のことだ。

### ddl-auto: update とは

ddl-auto を update とした場合は、テーブルやカラムが増えた場合のみ、DDL を発行する。

例えば、ユニークキーの付いた新規カラム name_pa を Color モデルに追加して、
ddl-auto: update でプロジェクトを起動すると、以下の DDL が発行されることがログで確認できる。

alter はテーブルの定義を変更する DDL だ。

``` console
Hibernate: alter table colors add column name_pa varchar(255) not null
Hibernate: alter table colors drop constraint if exists UK_csrybj58cvkd59ioakfm8qtmh
Hibernate: alter table colors add constraint UK_csrybj58cvkd59ioakfm8qtmh unique (name_pa)
```

ddl-auto: update は、新たなテーブル、カラムの追加はしてくれるものの、カラムサイズの変更、テーブル・カラムの削除などした場合には DDL を発行してくれない。

### ddl-auto: create-drop とは

一方、ddl-auto: create-drop を設定しておくと、プロジェクトを起動した際に、全テーブルの削除、作成の DDL を発行してくれる。

以下が、ddl-auto: create-drop を設定してプロジェクトを起動した場合に発行される DDL だ。
発行される DDL はログで確認できる。

``` console
Hibernate: drop table if exists colors CASCADE 
Hibernate: drop table if exists vegitables CASCADE 
Hibernate: create table colors (id bigint generated by default as identity, created_at timestamp not null, name_en varchar(255) not null, name_ja varchar(255) not null, updated_at timestamp not null, primary key (id))
Hibernate: alter table colors add constraint UK_9b5mld9tga7vdolutvf6gamce unique (name_en)
Hibernate: alter table colors add constraint UK_46uyihludctbqclftp1ctkb2p unique (name_ja)
```

### よくあるトラブルに陥らないためにも

ddl-auto の設定を変えることで、モデルに変更を加えた際にプロジェクト起動時、Hibernate がどんな DDL が発行するのか、色々試してもらいたい。

「モデルに加えたはずの変更が効いていない！」というトラブルは、開発現場でも時々聞く。

そんなトラブルに陥らないためにも、ddl-auto の設定、挙動は理解しておくと良い。

## API を叩いてみる

ddl-auto: create-drop でプロジェクトが起動したら、API を叩いてみる。

### Color API

まず、`POST http://localhost:8080/color/` を叩いて「赤」を COLORS テーブルに登録してみる。

id = 1 で Color を登録できた。

`API の実行結果:`
``` json
HTTP/1.1 201 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 10 Nov 2022 15:08:51 GMT
Connection: close

{
  "id": 1,
  "nameJa": "赤",
  "nameEn": "red",
  "createdAt": "2022-11-11T00:08:51.3122635",
  "updatedAt": "2022-11-11T00:08:51.3122635",
  "vegitableList": null
}
```

登録できたら、今度は `GET http://localhost:8080/color/1` を叩く。
id = 1 を指定して、対象の Color を取得する API だ。

以下のように登録された「赤」が取得できるはずだ。

``` java
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Fri, 11 Nov 2022 12:01:54 GMT
Connection: close

{
  "id": 1,
  "nameJa": "赤",
  "nameEn": "red",
  "createdAt": "2022-11-11T00:08:51.3122635",
  "updatedAt": "2022-11-11T00:08:51.3122635",
  "vegitableList": []
}
```

### Vegitable API

続いて、VegitableController の API を、
vegitable-api.http を使って叩いていく。

まずは、`POST http://localhost:8080/vegitable/` を叩いて「赤」を VEGITABLES テーブルに登録してみる。

ただし、元々のリクエストボディでは、正しく登録されないので、
変更した Vegitable モデルに合わせた形のリクエストボディに変更する。

`元々のリクエストボディ`
``` json
POST http://localhost:8080/vegitable/
content-type: application/json

{
  "name": "赤パプリカ",
  "color": "赤",
  "price": "100"
}
```

`変更した Vegitable モデルに合わせたリクエストボディ`
``` json
POST http://localhost:8080/vegitable/
content-type: application/json

{
  "name": "赤パプリカ",
  "price": "100",
  "color": {
    "id": 1
  }
}
```

id =1 の Color を紐づけて、Vegitable のレコードを1件登録する形だ。

登録が成功すれば、以下のような結果が返ってくる。

``` json
HTTP/1.1 201 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Fri, 11 Nov 2022 13:09:28 GMT
Connection: close

{
  "id": 1,
  "name": "赤パプリカ",
  "price": 100,
  "createdAt": "2022-11-11T22:09:28.6626278",
  "updatedAt": "2022-11-11T22:09:28.6626278",
  "color": {
    "id": 1,
    "nameJa": null,
    "nameEn": null,
    "createdAt": null,
    "updatedAt": null,
    "vegitableList": null
  }
}
```

### Vegitable API

では登録した Vegitable を取得してみる。

`GET http://localhost:8080/vegitable/list` を叩いて、登録した Vegitable を全件取得してみる。

そうすると何が起こるか？コンソールのログを見てほしい。

1080 行ほどのログがざっと、一気に吐かれる。

ログを追っていくと、以下のような Exception がある。

``` txt
com.fasterxml.jackson.databind.JsonMappingException: Infinite recursion (StackOverflowError)
```

Infinite recursion（無限再帰）、つまり参照先が参照先を参照し・・・といった状態が発生している。

鏡に映る自分が持つ鏡が、自分を映して、その自分が持つ鏡が自分を映し・・・という無限ループだ。

その結果が、無限に続くログに表示された Exception であり、最終的にメモリを使い果たし、StackOverflowError（メモリあふれエラー）となる。

そのことは、以下の挙動を見ると確信が持てる。

さきほどは、うまく取得できた Color API の `GET http://localhost:8080/color/1` を叩いてみる。

Vegitable API と同じように、Infinite recursion が起こっている。

つまり、Color 「赤」に紐づく Vegitable がまだ登録される前は、Color の参照先がないから Infinite recursion が起こらないが、
Vegitable 「赤パプリカ」が Color「赤」に紐づけて登録されると状況が変わってくる。

Color「赤」を取得した際に、Color「赤」に紐づいた Vegitable 「赤パプリカ」が取得され、
Vegitable 「赤パプリカ」に紐づいた Color「赤」が取得され・・・といったことが永遠に繰り返されオーバーフローが起こる。

### H2 管理コンソールで確認してみる

VEGITABLES テーブルと COLORS テーブルに、レコードが紐づけられた形で、
それぞれのテーブルに1件ずつレコードが登録されていることは H2 コンソールでも確認することができる。

H2 コンソールは、ブラウザで `http://localhost:8080/h2-ui/` にアクセスすることで見ることができる。

このコンソールの URL は、application.yml の `spring.h2.console.path: /h2-ui` で設定している。

管理コンソールにログインする username と password も、application.yml の `spring.datasource` に設定した値となる。

管理コンソールにログインしたら、サイドバーのでテーブルを選択してみる。

`COLORS` テーブルを選択すると、中央のエディタに `SELECT * FROM COLORS` と、
COLORS テーブルの全件を取得する SQL が表示される。

この状態でエディタ上部に配置されたボタンのうち、「Run」ボタンをクリックすると、SQL が実行され、
エディタの下に COLORS テーブルのレコードが表示される。

「赤」1件が登録されているのが分かると思う。

次に、エディタ上部に配置されたボタンのうち、「Clear」ボタンをクリックしてエディタの表示内容を消去した上で、
サイドバーから VEGITABLES テーブルを選択する。

そしてエディタに SQL `SELECT * FROM VEGITABLES` が表示された状態で「Run」ボタンをクリックする。

そうすると、こちらも「赤パプリカ」1件が登録されているのが分かる。

つまり、登録は正常に行われているが、Infinite recursion によって、API でのレコードの取得がうまくいっていない状態になっている。

ここから、この Infinite recursion を解決していこう。

## Json にシリアル化されないようにする

Infinite recursion の防止策として、単純なのは Json にシリアル化しないことである。

シリアル化（シリアライズ）というと難しいが、分かりやすく言うと、
Java オブジェクトを Json に変換することだ。

このシリアル化する際に、無限に参照が行われるため、メモリのオーバーフローを起こすのが問題となっている。

だから、シリアル化をしないようにすれば問題が起こらなくなるというわけだ。

シリアル化を無効にするには、@JsonManagedReference と @JsonBackReference を使う。

### @JsonManagedReference

@JsonManagedReference は、参照元、親側のモデル、
今回の例では Color モデルの `List<Vegitable> vegitableList` に付与する。

そうすると、@JsonManagedReference を付けられた側は、常にシリアル化される。

シリアル化で問題となっているのは、シリアル化されたフィールドがお互いに参照し続けることである。

この参照を断ち切って、参照元だけでシリアル化させようというのが @JsonManagedReference だ。

`Color モデルの List<Vegitable> vegitableList に @JsonManagedReference を付与`
``` java
@OneToMany(
	mappedBy="color",
	fetch = FetchType.LAZY,
	cascade = CascadeType.ALL,
	orphanRemoval = true)
@JsonManagedReference 
private List<Vegitable> vegitableList;
```

### @JsonBackReference

そして、参照する側、子側のモデル、
今回の例でいうと Vegitable モデルのフィールド `Color color` には @JsonBackReference を付与する。

@JsonBackReference を付けることで、そのフィールドはシリアル化されなくなる。

`Vegitable モデルの Color color に @JsonBackReference を付与`
``` java
@ManyToOne
@JoinColumn(name = "color_id") 
@JsonBackReference
private Color color;
```

## 再度 API を叩いてみる

@JsonManagedReference と @JsonBackReference を付けて、両サイドでのシリアル化を断ち切った上で、
再度 API を叩いてみる。

`POST http://localhost:8080/color/` で、「赤」を登録する。

続いて、`POST http://localhost:8080/vegitable/` で先ほどと同じように、
リクエストボディで Color の id = 1 で紐づけて Vegitable「赤パプリカ」を登録する。

そして、次は先ほどは Infinite recursion に陥った、Vegitable の取得を行う。

### 参照する側 Vegitable の取得結果 

`GET http://localhost:8080/vegitable/list` を叩く。

`取得結果:`
``` json
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Fri, 11 Nov 2022 14:22:10 GMT
Connection: close

[
  {
    "id": 1,
    "name": "赤パプリカ",
    "price": 100,
    "createdAt": "2022-11-11T23:22:08.32191",
    "updatedAt": "2022-11-11T23:22:08.32191"
  }
]
```

取得結果を見ると、color オブジェクトがシリアル化されていないのが分かる。

シリアル化されていれば、以下のようななるはずだが、そのようにはなっていない。

`Vegitable モデルの color フィールドがシリアル化されていれば:`
``` json
[
  {
    "id": 1,
    "name": "赤パプリカ",
    "price": 100,
    "createdAt": "2022-11-11T23:22:08.32191",
    "updatedAt": "2022-11-11T23:22:08.32191"
    "color": {
        "id": 1,
        "nameJa": "赤",
        "nameEn": "red",
        "createdAt": "2022-11-11T23:22:04.708325",
        "updatedAt": "2022-11-11T23:22:04.708325",
        "vegitableList": [
            {
                // 無限に参照が続いてしまうため省略
            }
        ]
    }
  }
]
```

### 参照される側 Color の取得結果 

`GET http://localhost:8080/color/1` を叩く。

`取得結果:`
``` json
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Fri, 11 Nov 2022 14:29:44 GMT
Connection: close

{
  "id": 1,
  "nameJa": "赤",
  "nameEn": "red",
  "createdAt": "2022-11-11T23:22:04.708325",
  "updatedAt": "2022-11-11T23:22:04.708325",
  "vegitableList": [
    {
      "id": 1,
      "name": "赤パプリカ",
      "price": 100,
      "createdAt": "2022-11-11T23:22:08.32191",
      "updatedAt": "2022-11-11T23:22:08.32191"
    }
  ]
}
```

取得結果を見ると、vegitableList オブジェクトはシリアル化されているのが分かる。

vegitableList オブジェクトの1つ1つの vegitable オブジェクトは、
@JsonBackReference によってシリアル化が止まっているので、無限の参照は起きていない。

## VEGITABLES テーブルに COLOR_ID で登録したい

ただ、このように @JsonManagedReference と @JsonBackReference を使って、
循環参照の両側を断ち切ることで、親側のテーブル、子側のテーブルの登録・取得ができるようになった。

しかし、子側（VEGITABLES）テーブルに登録するときに、
以下のようにリクエストボディで、color.id の形で、親側（COLORS）テーブルの ID を指定しなければいけない。

`VEGITABLES テーブルに登録（POST）したいときのリクエストボディ`
``` json
{
  "name": "赤パプリカ",
  "price": "100",
  "color": {
    "id": 1
  }
}
```

また、VEGITABLES テーブルを取得（GET）した時に、
以下のように、VEGITABLES のレコードに紐づけられた、
COLORS のレコードが何なのか分からなくなる。

``` json
{
  "id": 1,
  "name": "赤パプリカ",
  "price": 100,
  "createdAt": "2022-11-12T10:18:42.059821",
  "updatedAt": "2022-11-12T10:18:42.059821"
}
```

ではどうしたいのかというと、以下の要件を満たしたい。

- VEGITABLES テーブルに登録（POST）する時は、colorId を指定して登録できる
- VEGITABLES テーブルを取得（GET）した時のレスポンスに、colorId が含まれるようにしたい。

それでは、この要件が満たせるように、実装を修正していこう。

##

まずは、Vegitable モデルに colorId フィールドを追加する。

`model/Vegitable.java（抜粋）`
``` java
// long 型の colorId フィールドを追加する
@Column(name = "COLOR_ID")
private long colorId;
```

しかし、これだけで動かそうとすると MappingException が発生する。

Exception のログを見ると以下のメッセージが出力されているはずだ。

``` txt
Repeated column in mapping for entity: 
com.example.vegih2api.model.Vegitable column: color_id
 (should be mapped with insert="false" update="false")
```

つまり、Vegitable モデルに、2つの color_id カラムが指定されているから、
`insert="false" update="false"` を使ってねと注意を受けている。

「このフィールドは、カラムに登録しない、更新もしないフィールドですよ」ということを、
片方のフィールドに設定してあげないといけないわけだ。

以下のように color フィールドのほうに `insert="false" update="false"` を設定する。

`model/Vegitable.java（抜粋）`
``` java
// long 型の colorId フィールドを追加する
@Column(name = "COLOR_ID")
private long colorId;

・・・中略・・・

@ManyToOne
@JoinColumn (name = "COLOR_ID", updatable = false, insertable = false)
@JsonBackReference
private Color color;
```

これで、colorId で Vegitable を登録できるはずだ。

では、`POST http://localhost:8080/color/` で、「赤」を登録した上で、
以下のように colorId を使うようにリクエストボディを変更して、
Vegitable の登録をしてみよう。

``` json
{
  "name": "赤パプリカ",
  "price": "100",
  "colorId": 1
  // "color": {
  //   "id": 1
  // 
}
```

以下のように colorId で登録できているはずだ。

``` json
HTTP/1.1 201 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 12 Nov 2022 01:47:20 GMT
Connection: close

{
  "id": 1,
  "name": "赤パプリカ",
  "colorId": 1,
  "price": 100,
  "createdAt": "2022-11-12T10:47:20.1779718",
  "updatedAt": "2022-11-12T10:47:20.1779718"
}
```

## Vegitable 取得時に、Color の情報が一緒に取れてくるようにしたい

この状態で Vegitable と Color のそれぞれを取得した際に、レスポンスがどんな形になるのか確認してみよう。

`VEGITABLES テーブルを取得（GET）したレスポンス`
``` json
{
  "id": 1,
  "name": "赤パプリカ",
  "colorId": 1,
  "price": 100,
  "createdAt": "2022-11-12T10:57:14.078978",
  "updatedAt": "2022-11-12T10:57:14.078978"
}
```

`COLORS テーブルを取得（GET）したレスポンス`
``` json
{
  "id": 1,
  "nameJa": "赤",
  "nameEn": "red",
  "createdAt": "2022-11-12T10:57:10.226143",
  "updatedAt": "2022-11-12T10:57:10.226143",
  "vegitableList": [
    {
      "id": 1,
      "name": "赤パプリカ",
      "colorId": 1,
      "price": 100,
      "createdAt": "2022-11-12T10:57:14.078978",
      "updatedAt": "2022-11-12T10:57:14.078978"
    }
  ]
}
```

現状は、Vegitable 取得時には、Vegitable に紐づく Color は返ってこない。

一方で、Color 取得時には、Color のレコードに紐づく Vegitable が返ってくる。

やりたいことは、この逆だ。

- Color を取得した際には、その Color レコードにどの Vegitable が紐づいているか知る必要がない
- Vegitable を取得した際には、その Vegitable レコードにどの Color が紐づいているか知る必要がある

というわけで、1つずつ要件を満たしていこう。

## Color モデルのシリアル化を止める

Color モデルのシリアル化を止めることで以下の要件は満たせる。

- Color を取得した際には、その Color レコードにどの Vegitable が紐づいているか知る必要がない

つまり、修正は、Color モデルの対象フィールドを `@JsonBackReference` を付けるだけだ。

元々、`List<Vegitable> vegitableList` フィールドには、
シリアル化をするために `@JsonManagedReference` が付与されているので、これを削除する。

`model/Color.java（抜粋）`
``` java
@OneToMany(
	mappedBy = "color", 
	fetch = FetchType.LAZY, 
	cascade = CascadeType.ALL, 
	orphanRemoval = true)
// @JsonManagedReference は削除する
// @JsonBackReference でこのフィールドをシリアル化しないようにする
@JsonBackReference
private List<Vegitable> vegitableList;
```

これで、Color モデル側は要件を満たせるようになった。

Color を登録、取得してレスポンスを確かめてみよう。

`Color モデルの Vegitable モデルに紐づくフィールドのシリアル化を止めた結果:`
``` json
{
  "id": 1,
  "nameJa": "赤",
  "nameEn": "red",
  "createdAt": "2022-11-12T11:33:41.078016",
  "updatedAt": "2022-11-12T11:33:41.078016"
}
```

## Vegitable モデルにシリアル化を有効にする

Vegitable モデルのシリアル化を有効にすることで以下の要件は満たせる。

- Vegitable を取得した際には、その Vegitable レコードにどの Color が紐づいているか知る必要がある

こちらもやることは簡単で、Vegitable モデルの対象フィールドに付与していた `@JsonBackReference` を削除するだけだ。

`model/Vegitable.java（抜粋）`
``` java
@ManyToOne
@JoinColumn (name = "color_id", updatable = false, insertable = false)
// @JsonBackReference は削除する
private Color color;
```

これで、Vegitable モデル側も要件を満たせるようになった。

Color を登録、Vegitable を登録、取得してレスポンスを確かめてみよう。

`Vegitable モデルの Color モデルに紐づくフィールドのシリアル化を有効にした結果:`
``` json
{
  "id": 1,
  "name": "赤パプリカ",
  "colorId": 1,
  "price": 100,
  "createdAt": "2022-11-12T11:36:58.694454",
  "updatedAt": "2022-11-12T11:36:58.694454",
  "color": {
    "id": 1,
    "nameJa": "赤",
    "nameEn": "red",
    "createdAt": "2022-11-12T11:36:55.545724",
    "updatedAt": "2022-11-12T11:36:55.545724"
  }
}
```

これで多対 1（0 以上多対 1）のモデルは完成したので、
各種 API が動くようにひと通り実装を追加してプロジェクトを仕上げていく。

## 各種 API が動くようにひと通り実装を追加

### repository/ColorRepository.java

``` java
public interface ColorRepository extends JpaRepository<Color, Long> {
    List<Color> findByNameJaContaining(String nameJa);
    List<Color> findByNameEnContaining(String nameEn);
}
```

### service/ColorService.java

``` java
@Service
public class ColorService {

    @Autowired
    ColorRepository colorRepository;

    public List<Color> getAll(String nameJa, String nameEn) {
        List<Color> colorList = new ArrayList<Color>();

        if (nameJa == null && nameEn == null)
            this.colorRepository.findAll().forEach(colorList::add);
        else if (nameJa != null)
            this.colorRepository.findByNameJaContaining(nameJa).forEach(colorList::add);
        else
            this.colorRepository.findByNameEnContaining(nameEn).forEach(colorList::add);
        return colorList;
    }

    public Color getById(long id) {
        Optional<Color> colorData = this.colorRepository.findById(id);

        if (colorData.isPresent())
            return colorData.get();
        return null;
    }

    public Color create(Color color) {
        LocalDateTime now = LocalDateTime.now();

        color.setCreatedAt(now);
        color.setUpdatedAt(now);
        return this.colorRepository.save(color);
    }

    public Color update(long id, Color color) throws Exception {
        Optional<Color> colorData = this.colorRepository.findById(id);

        if (colorData.isPresent()) {
            Color resultColor= colorData.get();
            resultColor.setNameJa(color.getNameJa());
            resultColor.setNameEn(color.getNameEn());

            LocalDateTime now = LocalDateTime.now();
            resultColor.setUpdatedAt(now);
            return this.colorRepository.save(resultColor);
        } else {
            throw new Exception();
        }
    }

    public void deletedById(long id) throws Exception {
        Optional<Color> colorData = this.colorRepository.findById(id);

        if (colorData.isPresent()) {
            this.colorRepository.deleteById(id);
            return;
        }
        throw new Exception();
        
    }

    public void deleteAll() throws Exception {
        this.colorRepository.deleteAll();
    }
}
```

### controller/ColorController.java

``` java
@RestController
@RequestMapping("/color")
public class ColorController {

    @Autowired
    ColorService colorService;

    @GetMapping("/list")
    public ResponseEntity<List<Color>> getAll(@RequestParam(required = false) String nameJa,
            @RequestParam(required = false) String nameEn) {
        if (nameJa != null && nameEn != null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try {
            List<Color> colorList = this.colorService.getAll(nameJa, nameEn);

            if (colorList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(colorList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Color> getById(@PathVariable("id") long id) {
        Color color = this.colorService.getById(id);

        if (color != null)
            return new ResponseEntity<>(color, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/")
    public ResponseEntity<Color> create(@RequestBody Color color) {
        try {
            Color resultColor = this.colorService.create(color);
            return new ResponseEntity<>(resultColor, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Color> update(@PathVariable("id") long id, @RequestBody Color color) {
        try {
            Color resultColor = this.colorService.update(id, color);
            return new ResponseEntity<>(resultColor, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") long id) {
        try {
            this.colorService.deletedById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<HttpStatus> deleteAll() {
        try {
            this.colorService.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
```

### rest-client/color-api.http

``` md
### getAll()
GET http://localhost:8080/color/list

### getAll(nameJa)
GET http://localhost:8080/color/list?nameJa=赤

### getAll(nameEn)
GET http://localhost:8080/color/list?nameEn=red

### getById(id)
GET http://localhost:8080/color/1

### create(color)
POST http://localhost:8080/color/
content-type: application/json

{
  "nameJa": "赤",
  "nameEn": "red"
}

### update(id, color)
PUT http://localhost:8080/color/1
content-type: application/json

{
  "nameJa": "赤",
  "nameEn": "fire"
}

### delete(id)
DELETE http://localhost:8080/color/1

### deleteAll()
DELETE http://localhost:8080/color/
```

## API 作成の達人になるために

この後は、色々処理を変えてみたり、機能を追加したり、
またはゼロから完成まで手を止めずに何度も作ったり。

そうすれば最初は難しかった API も、息を吸うように軽く作成できるようになるだろう。

また、たくさん作っていくうちに、試行錯誤していくうちに、
ログを追いかけて、変更前と変更後がどう違うかを確認していくうちに、
API の理解がぐっと深まっていくと思う。

目指せ達人！
