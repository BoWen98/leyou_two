package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.BrandClient;
import com.leyou.item.client.CategoryClient;
import com.leyou.item.client.GoodsClient;
import com.leyou.item.client.SpecificationClient;
import com.leyou.pojo.*;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    /**
     * 把一个Spu对象转为一个Goods对象
     *
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu) {
        // 查询分类
        List<Long> idList = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<Category> categoryList = categoryClient.queryCategoryByIds(idList);
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<String> names = new ArrayList<>();
        for (Category category : categoryList) {
            names.add(category.getName());
        }
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        // 搜索字段
        String all = spu.getTitle() + org.apache.commons.lang.StringUtils.join(names, " ") + brand.getName();

        // 查询sku
        Long spuId = spu.getId();
        List<Sku> skuList = goodsClient.querySkuBySpuId(spuId);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        Set<Long> priceSet = new HashSet<>();
        // 对sku进行处理, 只取部分需要的字段
        List<Map<String, Object>> skus = new ArrayList<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("images", StringUtils.substringBefore(sku.getImages(), ","));
            skus.add(map);
            // 处理价格
            priceSet.add(sku.getPrice());
        }
        // 查询规格参数
        List<SpecParam> params =
                specificationClient.querySpecParam(spu.getCid3(), null, true);
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        // 查询商品详情
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuId);
        // 获取通用规格参数
        String genericJson = spuDetail.getGenericSpec();
        Map<Long, String> genericSpec = JsonUtils.toMap(genericJson, Long.class, String.class);
        // 获取特有规格参数
        String specialJson = spuDetail.getSpecialSpec();
        Map<Long, List<String>> specialSpec =
                JsonUtils.nativeRead(specialJson, new TypeReference<Map<Long, List<String>>>() {
                });
        // 规格参数, key是规格参数的名字, 值是规格参数的值
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            String key = param.getName();
            Object value = "";
            if (param.getGeneric()) {
                value = genericSpec.get(param.getId());
                // 判断是否是数值类型
                if (param.getNumeric()) {
                    // 处理成段
                    value = chooseSegment(value.toString(), param);
                }
            } else {
                value = specialSpec.get(param.getId());
            }
            // 存入map
            specs.put(key, value);
        }
        // 构建goods对象
        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(all);
        goods.setSkus(JsonUtils.toString(skus));
        goods.setPrice(priceSet);
        goods.setSpecs(specs);
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }


    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 搜索功能
     *
     * @param request
     * @return
     */
/*
    public PageResult<Goods> search(SearchRequest request) {
        //获取搜索条件
        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 1.结果过滤字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));

        //2. 分页
        int page = request.getPage() - 1;
        int size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page, size));

        //3.搜索条件
        //构建基本查询
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);

        //4.排序
        if (StringUtils.isNotBlank(request.getSortBy())) {
            //如果排序字段不为空,则排序
            queryBuilder.withSort(SortBuilders.fieldSort(request.getSortBy()).order(request.getDescending() ? SortOrder.DESC : SortOrder.ASC));
        }

        //5.对分类和品牌聚合
        //5.1分类
        String categoryAggName = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //5.2品牌
        String brandAggName = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //6.搜索查询结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        //7.解析结果
        //7.1解析聚合结果
        //7.1.1准备过滤待选项
        List<Map<String, Object>> filterList = new ArrayList<>();
        //7.1.2获取所有聚合
        Aggregations aggregations = result.getAggregations();
        //7.1.3获取分类聚合
        LongTerms categoryTerm = aggregations.get(categoryAggName);
        //7.1.4解析分类聚合
        List<Long> idList = handleCategoryAgg(categoryTerm, filterList);
        //7.1.5获取品牌聚合
        LongTerms brandTerm = aggregations.get(brandAggName);
        //7.1.4解析品牌聚合
        handleBrandAgg(brandTerm, filterList);
        // 7.2 处理规格参数
        if (idList != null && idList.size() == 1) {
            // 当前分类有且只有一个，可以进行规格参数的聚合, 参数有3个：分类id，搜索的条件，过滤条件的集合
            handleSpecAgg(idList.get(0), basicQuery, filterList);
        }
        //7.2解析分页结果
        long total = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> list = result.getContent();

        // 8.封装结果并返回
        return new SearchResult(total, totalPages, list, filterList);
    }

    private void handleSpecAgg(Long cid, QueryBuilder basicQuery, List<Map<String, Object>> filterList) {
        // 1 先根据分类，查找可以用来搜索的规格
        List<SpecParam> params = specificationClient.querySpecParam(cid, null, true);
        // 2 在用户搜索结果的基础上，对规格参数进行聚合
        // 2.1 创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2.2 添加搜索条件
        queryBuilder.withQuery(basicQuery);
        // 2.3 添加聚合条件
        for (SpecParam param : params) {
            // 获取规格参数的名称，作为聚合的名称，方便将来根据名称获取聚合
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name));
        }
        // 2.4 设置size为最小，避免搜索的结果
        queryBuilder.withPageable(PageRequest.of(0, 1));
        // 3 聚合，获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        // 4 将规格参数聚合结果整理后返回
        //获取所有聚合
        Aggregations aggregations = result.getAggregations();
        // 遍历规格参数，根据参数名称，取出每个聚合
        for (SpecParam param : params) {
            // 根据名称，获取聚合结果
            StringTerms terms = aggregations.get(param.getName());
            // 获取聚合的buckets，作为过滤项
            List<String> options = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            // 准备过滤项的结构map,把过滤的key和过滤项放进去
            Map<String, Object> filter = new HashMap<>();
            filter.put("k", param.getName());
            filter.put("options", options);
            filterList.add(filter);
        }
    }
*/
    public PageResult<Goods> search(SearchRequest request) {
        int page = request.getPage() - 1;
        int size = request.getSize();
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // 分页
        queryBuilder.withPageable(PageRequest.of(page, size));
        // 过滤
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
        // 聚合分类和品牌
        // 聚合分类
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        // 聚合品牌
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 查询
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        // 解析结果
        long total = result.getTotalElements();
        int totalPage = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        // 解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));
        // 完成规格参数聚合
        List<Map<String, Object>> specs = null;
        if (categories != null && categories.size() == 1) {
            // 商品分类存在并且数量为1, 可以聚合规格参数
            specs = buildSpecificationAgg(categories.get(0).getId(), basicQuery);
        }
        return new SearchResult(total, totalPage, goodsList, categories, brands, specs);
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();
        // 查询需要聚合的规格参数
        List<SpecParam> params = specificationClient.querySpecParam(cid, null, true);
        // 聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(
                    AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        }
        // 获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : params) {
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            List<String> options = terms.getBuckets()
                    .stream()
                    .map(bucket -> bucket.getKeyAsString())
                    .collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", options);

            specs.add(map);
        }
        // 解析结果
        return specs;
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<LongTerms.Bucket> buckets = terms.getBuckets();
            List<Long> ids = new ArrayList<>();
            for (LongTerms.Bucket bucket : buckets) {
                long id = bucket.getKeyAsNumber().longValue();
                ids.add(id);
            }
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        } catch (Exception e) {
            log.error("[搜索服务] 查询分类异常: " + e);
            return null;
        }
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        List<Long> ids = new ArrayList<>();
        for (LongTerms.Bucket bucket : buckets) {
            long id = bucket.getKeyAsNumber().longValue();
            ids.add(id);
        }
        List<Brand> brands = brandClient.queryBrandByIds(ids);
        return brands;
    }


    private QueryBuilder buildBasicQuery(SearchRequest request) {
        // 1 创建布尔查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 2 添加搜索条件
        boolQuery.must(QueryBuilders.matchQuery("all", request.getKey()));
        // 3 添加过滤条件
        // 3.1 获取所有需要过滤的条件
        Map<String, String> filter = request.getFilter();
        // 3.2 循环添加过滤条件
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            // 过滤条件的key
            String key = entry.getKey();
            // 过滤条件的值
            String value = entry.getValue();
            // 如果不是分类或品牌，需要对key添加前缀
            if (!"cid3".equals(key) && !"brandId".equals(key)) {
                key = "specs." + key;
            }
            boolQuery.filter(QueryBuilders.termQuery(key, value));
        }
        return boolQuery;
    }

    private void handleBrandAgg(LongTerms brandTerm, List<Map<String, Object>> filterList) {
        // 解析聚合中的桶
        List<LongTerms.Bucket> buckets = brandTerm.getBuckets();
        // 把桶中的id取出，形成品牌的id集合
        List<Long> list = buckets.stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        // 根据id查询所有品牌
        List<Brand> brandList = brandClient.queryBrandByIds(list);
        // 准备过滤项的结构map,把过滤的key和过滤项放进去
        Map<String, Object> filter = new HashMap<>();
        filter.put("k", "brandId");
        filter.put("options", brandList);
        filterList.add(filter);
    }

    private List<Long> handleCategoryAgg(LongTerms categoryTerm, List<Map<String, Object>> filterList) {
        //解析聚合中的桶
        List<LongTerms.Bucket> buckets = categoryTerm.getBuckets();
        //把桶中的id取出,形成分类的id集合
        List<Long> idList = buckets.stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //根据id查询所有分类
        List<Category> categoryList = categoryClient.queryCategoryByIds(idList);
        // 准备过滤项的结构map,把过滤的key和过滤项放进去
        Map<String, Object> filter = new HashMap<>();
        filter.put("k", "cid3");
        filter.put("options", categoryList);
        filterList.add(filter);
        return idList;
    }

    //新增或修改索引库
    public void insertOrUpdateIndex(Long id) {
        //查询spu
        Spu spu = goodsClient.querySpuBySpuId(id);
        if (null == spu) {
            log.error("索引对应的spu不存在,spuId:{}", id);
            //抛出异常,让消息回滚
            throw new RuntimeException();
        }
        Goods goods = buildGoods(spu);
        //保存数据到索引库
        goodsRepository.save(goods);
    }

    //删除索引库
    public void deleteIndex(Long id) {
        goodsRepository.deleteById(id);
    }
}
