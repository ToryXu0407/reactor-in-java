package com.toryxu.reactor;


import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author toryxu
 * @version 1.0
 * @date 2020/9/24 9:19 下午
 */
public class reactorTest {

    @Test
    public void createAFlux_just() {
        Flux<String> fruitFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        //订阅并输出
//        fruitFlux.subscribe(f -> {
//            System.out.println("Here's some fruit:" + f);
//        });
        //验证预定义的数据是否流经了fruitFlux
        StepVerifier.create(fruitFlux).expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Grape")
                .expectNext("Banana")
                .expectNext("Strawberry")
                .verifyComplete();
    }

    @Test
    public void createAFlux_fromIterable() {
        List<String> fruitList = new ArrayList<>();
        fruitList.add("Apple");
        fruitList.add("Orange");
        fruitList.add("Grape");
        fruitList.add("Banana");
        fruitList.add("Strawberry");

        //迭代器转换fruitFlux
        Flux<String> fruitFlux = Flux.fromIterable(fruitList);

        StepVerifier.create(fruitFlux).expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Grape")
                .expectNext("Banana")
                .expectNext("Strawberry")
                .verifyComplete();
    }

    @Test
    public void createAFlux_fromStream() {
        Stream<String> fruitStream =
                Stream.of("Apple", "Orange", "Grape", "Banana", "Strawberry");

        //Java Stream
        //Stream（流）是一个来自数据源的元素队列并支持聚合操作
        Flux<String> fruitFlux = Flux.fromStream(fruitStream);

        StepVerifier.create(fruitFlux).expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Grape")
                .expectNext("Banana")
                .expectNext("Strawberry")
                .verifyComplete();
    }

    @Test
    public void createAFlux_range() {
        Flux<Integer> intervalFlux =
                Flux.range(1, 5);

        //range创建范围数据（start,count),接受integer值
        StepVerifier.create(intervalFlux)
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .expectNext(4)
                .expectNext(5)
                .verifyComplete();
    }

    @Test
    public void createAFlux_interval() {
        Flux<Long> intervalFlux =
                Flux.interval(Duration.ofSeconds(1))
                        .take(5);
        //每秒发布一个值，从0开始发布，没有指定最大值，通过take方法限制。
        StepVerifier.create(intervalFlux)
                .expectNext(0L)
                .expectNext(1L)
                .expectNext(2L)
                .expectNext(3L)
                .expectNext(4L)
                .verifyComplete();
    }

    //组合反应式类型
    @Test
    public void mergeFluxes() {
        Flux<String> characterFlux = Flux
                .just("Garfield", "Kojak", "Barbossa")
                //间隔时间
                .delayElements(Duration.ofMillis(500));
        Flux<String> foodFlux = Flux
                .just("Lasagna", "Lollipops", "Apples")
                //延迟发送
                .delaySubscription(Duration.ofMillis(250))
                .delayElements(Duration.ofMillis(500));

        Flux<String> mergedFlux = characterFlux.mergeWith(foodFlux);

        StepVerifier.create(mergedFlux)
                .expectNext("Garfield")
                .expectNext("Lasagna")
                .expectNext("Kojak")
                .expectNext("Lollipops")
                .expectNext("Barbossa")
                .expectNext("Apples")
                .verifyComplete();
    }

    @Test
    public void zipFluxes() {
        Flux<String> characterFlux = Flux
                .just("Garfield", "Kojak", "Barbossa");
        Flux<String> foodFlux = Flux
                .just("Lasagna", "Lollipops", "Apples");

        //zip，设置多值。
        Flux<Tuple2<String, String>> zippedFlux =
                Flux.zip(characterFlux, foodFlux);
        StepVerifier.create(zippedFlux)
                .expectNextMatches(p ->
                        p.getT1().equals("Garfield") &&
                                p.getT2().equals("Lasagna"))
                .expectNextMatches(p ->
                        p.getT1().equals("Kojak") &&
                                p.getT2().equals("Lollipops"))
                .expectNextMatches(p ->
                        p.getT1().equals("Barbossa") &&
                                p.getT2().equals("Apples"))
                .verifyComplete();
    }

    @Test
    public void firstFlux() {
        Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth")
                .delaySubscription(Duration.ofMillis(100));
        Flux<String> fastFlux = Flux.just("hare", "cheetah", "squirrel");
        //选择第一个发布消息的Flux并只发布该Flux的值。
        Flux<String> firstFlux = Flux.first(slowFlux, fastFlux);

        StepVerifier.create(firstFlux)
                .expectNext("hare")
                .expectNext("cheetah")
                .expectNext("squirrel")
                .verifyComplete();
    }

    @Test
    public void skipAFew() {
        //跳过指定数目的消息并将剩下的消息继续在结果Flux上进行传递。
        Flux<String> skipFlux = Flux.just(
                "one", "two", "skip a few", "ninety nine", "one hundred")
                .skip(3);

        StepVerifier.create(skipFlux)
                .expectNext("ninety nine", "one hundred")
                .verifyComplete();
    }

    @Test
    public void skipAFewSeconds() {
        //跳过某几秒前的数据
        Flux<String> skipFlux = Flux.just(
                "one", "two", "skip a few", "ninety nine", "one hundred")
                .delayElements(Duration.ofSeconds(1))
                .skip(Duration.ofSeconds(4));

        StepVerifier.create(skipFlux)
                .expectNext("ninety nine", "one hundred")
                .verifyComplete();
    }

    @Test
    public void takeNum() {
        Flux<String> nationalParkFlux = Flux.just(
                "Yellowstone", "Yosemite", "Grand Canyon",
                "Zion", "Grand Teton")
                //take 个数
                .take(3);
        StepVerifier.create(nationalParkFlux)
                .expectNext("Yellowstone", "Yosemite", "Grand Canyon")
                .verifyComplete();
    }

    @Test
    public void takeTime() {
        Flux<String> nationalParkFlux = Flux.just(
                "Yellowstone", "Yosemite", "Grand Canyon",
                "Zion", "Grand Teton")
                .delayElements(Duration.ofSeconds(1))
                //take 时间
                .take(Duration.ofMillis(3500));

        StepVerifier.create(nationalParkFlux)
                .expectNext("Yellowstone", "Yosemite", "Grand Canyon")
                .verifyComplete();
    }

    @Test
    public void filter() {
        Flux<String> nationalParkFlux = Flux.just(
                "Yellowstone", "Yosemite", "Grand canyon",
                "Zion", "Grand Teton")
                .filter(np -> !np.contains(" "));
        //filter过滤自定义条件
        StepVerifier.create(nationalParkFlux)
                .expectNext("Yellowstone", "Yosemite", "Zion")
                .verifyComplete();
    }

    @Test
    public void distinct() {
        //发布时去重
        Flux<String> animalFlux = Flux.just(
                "dog", "cat", "bird", "dog", "bird", "anteater"
        ).distinct();

        StepVerifier.create(animalFlux)
                .expectNext("dog", "cat", "bird", "anteater")
                .verifyComplete();
    }

    @Test
    public void map() {
        Flux<Player> playerFlux = Flux.just
                ("Michael Jordan", "Scottie Pippen", "Steve Kerr").
                map(n -> {
                    String[] spilt = n.split("\\s");
                    return new Player(spilt[0], spilt[1]);
                });
        //通过map拆分成对象。
        StepVerifier.create(playerFlux).expectNext(new Player("Michael", "Jordan"))
                .expectNext(new Player("Scottie", "Pippen"))
                .expectNext(new Player("Steve", "Kerr"))
                .verifyComplete();
    }

    @Test
    public void flatMap() {
        Flux<Player> playerFlux = Flux
                .just("Michael Jordan", "Scottie Pippen", "Steve Kerr")
                //flatMap里面是n个单对象
                //其每个订阅都在并行线程中完成
                .flatMap(n -> Mono.just(n)
                        .map(p -> {
                            String[] spilt = p.split("\\s");
                            return new Player(spilt[0], spilt[1]);
                            //Schedulers指定并发模型。
                            //如果里面的计算量比较大，无法区分哪个先完成
                        }).subscribeOn(Schedulers.parallel()));

        List<Player> playerList = Arrays.asList(new Player("Michael", "Jordan"),
                new Player("Scottie", "Pippen"), new Player("Steve", "Kerr"));

        StepVerifier.create(playerFlux).expectNext(playerList.get(0), playerList.get(1), playerList.get(2));
        StepVerifier.create(playerFlux)
                .expectNextMatches(playerList::contains)
                .expectNextMatches(playerList::contains)
                .expectNextMatches(playerList::contains)
                .verifyComplete();
    }

    @Test
    public void buffer() {
        Flux<String> fruitFlux = Flux.just(
                "apple", "orange", "banana", "kiwi", "strawberry"
        );
        //以3个分组。产生一个包含列表Flux
        Flux<List<String>> bufferedFlux = fruitFlux.buffer(3);

        StepVerifier.create(bufferedFlux)
                .expectNext(Arrays.asList("apple", "orange", "banana"))
                .expectNext(Arrays.asList("kiwi", "strawberry"))
                .verifyComplete();
    }

    @Test
    public void bufferAndFlatMap() {
        //将数据放到不同的list并且并行处理
        //比如之前的数据处理，不用手动处理并对每个分完组的数据开线程去跑了。
        Flux.just("apple", "orange", "banana", "kiwi", "strawberry")
                .buffer(3)
                .flatMap(x ->
                        Flux.fromIterable(x)
                                .map(String::toUpperCase)
                                .subscribeOn(Schedulers.parallel()).log()
                ).subscribe();
    }

    @Test
    public void collectList() {
        Flux<String> fruitFlux = Flux.just(
                "apple", "orange", "banana", "kiwi", "strawberry");
        //转到一个list的mono
        Mono<List<String>> fruitListMono = fruitFlux.collectList();

        StepVerifier.create(fruitListMono).expectNext(
                Arrays.asList("apple", "orange", "banana", "kiwi", "strawberry")).verifyComplete();
    }

    @Test
    public void collectMap() {
        Flux<String> animalFlux = Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo");

        Mono<Map<Character, String>> animalMapMono =
                animalFlux.collectMap(a -> a.charAt(0));
        //collectMap转成map
        //拿到map并作判断
        StepVerifier.create(animalMapMono).expectNextMatches(map -> {
            return map.size() == 3
                    && map.get('a').equals("aardvark")
                    && map.get('e').equals("eagle")
                    && map.get('k').equals("kangaroo");
        }).verifyComplete();
    }

    @Test
    public void all() {
        Flux<String> animalFlux = Flux.just(
                "aardvark", "elephant", "koala", "eagle", "kangaroo");

        //all是个判断，是否都成立
        Mono<Boolean> hasAMono = animalFlux.all(a -> a.contains("a"));

        StepVerifier.create(hasAMono).expectNext(true).verifyComplete();

        Mono<Boolean> hasKMono = animalFlux.all(a -> a.contains("k"));

        StepVerifier.create(hasKMono).expectNext(false).verifyComplete();
    }

    @Test
    public void any() {
        Flux<String> animalFlux = Flux.just(
                "aardvark", "elephant", "koala", "eagle", "kangaroo");

        //all是个判断，是否都成立
        Mono<Boolean> hasAMono = animalFlux.any(a -> a.contains("t"));

        StepVerifier.create(hasAMono).expectNext(true).verifyComplete();

        Mono<Boolean> hasKMono = animalFlux.any(a -> a.contains("z"));

        StepVerifier.create(hasKMono).expectNext(false).verifyComplete();
    }


}
