package com.etransportation.repository;

import com.etransportation.enums.BookStatus;
import com.etransportation.enums.CarStatus;
import com.etransportation.model.Car;
import com.etransportation.mybean.CarBean;
import com.etransportation.payload.dto.CarBrandDTO;
import com.etransportation.payload.dto.CarModelDTO;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {
  List<Car> findAllByStatusAndAddress_City_Code(CarStatus status, String code, Pageable pageable);

  //   @Query(
  //     nativeQuery = true,
  //     value = "SELECT * FROM Car WHERE id in (SELECT c.id FROM Car c INNER JOIN address a on a.id = c.id INNER JOIN city ci on ci.id = a.city_id LEFT JOIN book b on b.car_id = c.id  WHERE c.status = ?1 AND ci.code = ?2 GROUP BY c.id ORDER BY COUNT(c.id) DESC OFFSET 0 ROWS)",
  //     countQuery = "SELECT count(*) FROM Car WHERE id in (SELECT c.id FROM Car c INNER JOIN address a on a.id = c.id INNER JOIN city ci on ci.id = a.city_id LEFT JOIN book b on b.car_id = c.id  WHERE c.status = ?1 AND ci.code = ?2 GROUP BY c.id ORDER BY COUNT(c.id) DESC OFFSET 0 ROWS)"
  //   )
  //   Page<Car> findCarByCityCodeSortByCountBookOfCar(String CarStatus, String code, Pageable pageable);

  //mysql khong co  OFFSET 0 ROWS
  @Query(
    nativeQuery = true,
    value = "SELECT * FROM Car WHERE id in (SELECT c.id FROM Car c INNER JOIN address a on a.id = c.id INNER JOIN city ci on ci.id = a.city_id LEFT JOIN book b on b.car_id = c.id  WHERE c.status = ?1 AND ci.code = ?2 GROUP BY c.id ORDER BY COUNT(c.id) DESC)",
    countQuery = "SELECT count(*) FROM Car WHERE id in (SELECT c.id FROM Car c INNER JOIN address a on a.id = c.id INNER JOIN city ci on ci.id = a.city_id LEFT JOIN book b on b.car_id = c.id  WHERE c.status = ?1 AND ci.code = ?2 GROUP BY c.id ORDER BY COUNT(c.id) DESC)"
  )
  Page<Car> findCarByCityCodeSortByCountBookOfCar(String CarStatus, String code, Pageable pageable);

  List<Car> findAllByAccount_Id(Long id, Sort sort);

  Page<Car> findAllByStatus(CarStatus status, Pageable pageable);

  //   @Query(
  //     nativeQuery = true,
  //     value = "SELECT * FROM Car WHERE id in (SELECT c.id FROM Car c INNER JOIN  book b ON b.car_id = c.id LEFT JOIN review r ON r.id = b.id WHERE c.status = :status GROUP BY c.id HAVING count(c.id) >= 3 AND AVG(r.star_review) >= 3 ORDER BY count(c.id) DESC OFFSET 0 ROWS )",
  //     countQuery = "SELECT count(*) FROM Car WHERE id in (SELECT c.id FROM Car c INNER JOIN  book b ON b.car_id = c.id LEFT JOIN review r ON r.id = b.id WHERE c.status = :status GROUP BY c.id HAVING count(c.id) >= 3 AND AVG(r.star_review) >= 3 ORDER BY count(c.id) DESC OFFSET 0 ROWS )"
  //   )
  //   Page<Car> findCarByFamous(@Param("status") String statusCar, Pageable pageable);

  @Query(
    nativeQuery = true,
    value = "SELECT * FROM Car WHERE id in (SELECT c.id FROM Car c INNER JOIN  book b ON b.car_id = c.id LEFT JOIN review r ON r.id = b.id WHERE c.status = :status GROUP BY c.id HAVING count(c.id) >= 3 AND AVG(r.star_review) >= 3 ORDER BY count(c.id) DESC)",
    countQuery = "SELECT count(*) FROM Car WHERE id in (SELECT c.id FROM Car c INNER JOIN  book b ON b.car_id = c.id LEFT JOIN review r ON r.id = b.id WHERE c.status = :status GROUP BY c.id HAVING count(c.id) >= 3 AND AVG(r.star_review) >= 3 ORDER BY count(c.id) DESC)"
  )
  Page<Car> findCarByFamous(@Param("status") String statusCar, Pageable pageable);

  @Query(
    "SELECT new com.etransportation.payload.dto.CarBrandDTO(br.id, br.name, count(br.id))" +
    " FROM CarBrand br JOIN br.carModels mo JOIN mo.cars c" +
    " WHERE c.id in ?1" +
    " GROUP BY br.id, br.name" +
    " ORDER BY count(br.id) DESC"
  )
  List<CarBrandDTO> findAllBrandAndCountByFilterSearch(Long[] carId);

  @Query(
    "SELECT new com.etransportation.payload.dto.CarModelDTO(mo.id, mo.name, count(mo.id))" +
    " FROM CarBrand br JOIN br.carModels mo JOIN mo.cars c" +
    " WHERE c.id in ?1" +
    " GROUP BY mo.id, mo.name, br.id" +
    " HAVING br.id = ?2" +
    " ORDER BY count(mo.id) DESC"
  )
  List<CarModelDTO> findAllModelAndCountByFilterSearch(Long[] carId, Long _BrandId);

  Page<Car> findAllByLikeAccounts_Id(Long accountId, Pageable pageable);

  Boolean existsByIdAndBooks_EndDateGreaterThanEqualAndBooks_Status(Long id, Date date, BookStatus status);
}
