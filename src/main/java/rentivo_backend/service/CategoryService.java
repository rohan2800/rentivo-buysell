package rentivo_backend.service;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import rentivo_backend.dto.CategoryDtos.*; import rentivo_backend.entity.*; import rentivo_backend.repository.*; import java.util.*;
@Service public class CategoryService { private final CategoryRepository cats; private final CategoryFieldRepository fields; public CategoryService(CategoryRepository c,CategoryFieldRepository f){cats=c;fields=f;}
 public List<Category> active(){return cats.findByActiveTrueOrderByNameAsc();}
 @Transactional public Category save(CategoryRequest r,Long id){Category c=id==null?new Category():cats.findById(id).orElseThrow(()->new IllegalArgumentException("Category not found")); c.setName(r.name().trim());c.setActive(r.active()); if(id!=null)c.getFields().clear(); if(r.fields()!=null)for(FieldRequest x:r.fields()){CategoryField f=new CategoryField();f.setCategory(c);f.setName(x.name());f.setType(x.type());f.setRequired(x.required());f.setOptionsCsv(x.optionsCsv());f.setSortOrder(x.sortOrder()==null?0:x.sortOrder());c.getFields().add(f);} return cats.save(c);}
 public Category get(Long id){return cats.findById(id).orElseThrow(()->new IllegalArgumentException("Category not found"));}
 public void active(Long id,boolean v){Category c=get(id);c.setActive(v);cats.save(c);} }
