package com.etransportation.service.impl;

import static com.etransportation.filter.CarSpecification.filterSearchCar;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etransportation.enums.BookStatus;
import com.etransportation.enums.CarStatus;
import com.etransportation.enums.RoleAccount;
import com.etransportation.model.Account;
import com.etransportation.model.Address;
import com.etransportation.model.Car;
import com.etransportation.model.CarBrand;
import com.etransportation.model.CarImage;
import com.etransportation.model.Feature;
import com.etransportation.model.Review;
import com.etransportation.model.Ward;
import com.etransportation.payload.dto.CarModelDTO;
import com.etransportation.payload.request.CarBrowsingRequest;
import com.etransportation.payload.request.CarRegisterRequest;
import com.etransportation.payload.request.CarUpdateInfoRequest;
import com.etransportation.payload.request.FilterCarSearchRequest;
import com.etransportation.payload.request.PagingRequest;
import com.etransportation.payload.response.CarBrandResponse;
import com.etransportation.payload.response.CarDetailInfoResponse;
import com.etransportation.payload.response.CarShortInfoResponse;
import com.etransportation.payload.response.PagingResponse;
import com.etransportation.payload.response.ReviewByCarResponse;
import com.etransportation.repository.AccountRepository;
import com.etransportation.repository.AddressRepository;
import com.etransportation.repository.CarBrandRepository;
import com.etransportation.repository.CarImageRepository;
import com.etransportation.repository.CarRepository;
import com.etransportation.repository.ReviewRepository;
import com.etransportation.repository.WardRepository;
import com.etransportation.service.CarService;
import com.etransportation.util.DateConverter;

@Service
public class CarServiceImpl implements CarService {

        @Autowired
        private CarBrandRepository carBrandRepository;

        @Autowired
        private ModelMapper modelMapper;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private WardRepository wardRepository;

        @Autowired
        private CarRepository carRepository;

        @Autowired
        private CarImageRepository carImageRepository;

        @Autowired
        private AddressRepository addressRepository;

        @Autowired
        private ReviewRepository reviewRepository;

        @Override
        @Transactional
        public List<CarBrandResponse> findAllCarBrands() {
                List<CarBrand> carBrand = carBrandRepository.findAll();
                List<CarBrandResponse> carBrandResponse = carBrand.stream().map(cb -> {
                        CarBrandResponse response = new CarBrandResponse();
                        response.setId(cb.getId());
                        response.setName(cb.getName());
                        response.setCarModels(cb.getCarModels().stream().map(cm -> {
                                CarModelDTO carModelDTO = new CarModelDTO();
                                carModelDTO.setId(cm.getId());
                                carModelDTO.setName(cm.getName());
                                return carModelDTO;
                        }).collect(Collectors.toList()));
                        return response;
                }).collect(Collectors.toList());
                return carBrandResponse;
        }

        @Override
        @Transactional
        public void save(CarRegisterRequest carRegisterRequest) {
                Account account = accountRepository.findById(carRegisterRequest.getAccount().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Account is not found!"));
                if (account.getRoles().stream().anyMatch(r -> r.getName() == RoleAccount.ADMIN)) {
                        throw new IllegalArgumentException("Chỉ user mới được đăng ký xe!");
                }

                Car car = modelMapper.map(carRegisterRequest, Car.class);
                Ward ward = wardRepository.findById(carRegisterRequest.getWard().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Chưa nhập địa chỉ"));
                Address address = Address
                                .builder()
                                .ward(ward)
                                .district(ward.getDistrict())
                                .city(ward.getDistrict().getCity())
                                .street(carRegisterRequest.getStreet())
                                .build();
                car.setAddress(address);
                car.setStatus(CarStatus.PENDING_APPROVAL);
                // nen dung @PrePersist cho ham duoi --> tim hieu trong class car
                // car.getCarImages().forEach(c -> c.setCar(car));
                carRepository.save(car);
        }

        @Override
        @Transactional
        public CarDetailInfoResponse findCarDetailInfo(Long carId) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
                Car car = carRepository.findById(carId)
                                .orElseThrow(() -> new IllegalArgumentException("Car not found"));
                CarDetailInfoResponse carDetailInfoResponse = modelMapper.map(car, CarDetailInfoResponse.class);

                carDetailInfoResponse.setName(car.getModel().getName() + " " + car.getYearOfManufacture());
                carDetailInfoResponse.setAddressInfo(
                                car.getAddress().getDistrict().getName() + ", " + car.getAddress().getCity().getName());
                carDetailInfoResponse.getBooks().removeIf(book -> book.getEndDate().before(cal.getTime())
                                || book.getStatus().equals(BookStatus.CANCEL));

                carDetailInfoResponse.getBooks().forEach(book -> {
                        book.setDates(DateConverter.getDatesBetween(book.getStartDate(), book.getEndDate()));

                });
                carDetailInfoResponse.setTotalBook(car.getBooks().size());

                List<Review> review = reviewRepository.findAllByBook_Car_Id(car.getId());

                carDetailInfoResponse.setTotalRating(Double.valueOf(
                                new DecimalFormat("#0.0").format(review.stream()
                                                .mapToInt(r -> r.getStarReview()).average()
                                                .orElse(0.0))));
                return carDetailInfoResponse;

        }

        @Override
        @Transactional
        public List<CarShortInfoResponse> findAllCarByUserId(Long id) {
                List<Car> cars = carRepository.findAllByAccount_Id(id, Sort.by("registerDate").descending());
                List<CarShortInfoResponse> listCarInfoResponse = cars.stream().map(c -> {
                        CarShortInfoResponse carInfoResponse = modelMapper.map(c, CarShortInfoResponse.class);
                        carInfoResponse.setAddressInfo(c.getAddress().getDistrict().getName() + ", "
                                        + c.getAddress().getCity().getName());
                        carInfoResponse.setName(c.getModel().getName() + " " + c.getYearOfManufacture());
                        if (c.getCarImages().size() == 0) {
                                carInfoResponse.setCarImage(
                                                "https://assetscdn1.paytm.com/images/catalog/product/K/KI/KIDLIMOUSINE-DISKSE1140676506C2066/1565610984539_0..jpg");
                        } else {
                                carInfoResponse.setCarImage(c.getCarImages()
                                                .get(new Random().nextInt(c.getCarImages().size()))
                                                .getImage());
                        }

                        carInfoResponse.setTotalBook(c.getBooks().size());
                        List<Review> review = reviewRepository.findAllByBook_Car_Id(c.getId());

                        carInfoResponse.setTotalRating(Double.valueOf(
                                        new DecimalFormat("#0.0").format(review.stream()
                                                        .mapToInt(r -> r.getStarReview()).average()
                                                        .orElse(0.0))));
                        return carInfoResponse;
                }).collect(Collectors.toList());

                return listCarInfoResponse;
        }

        @Override
        @Transactional
        public Object findAllCarByAdmin(PagingRequest pagingRequest) {

                Pageable pageable = PageRequest.of(pagingRequest.getPage() - 1, pagingRequest.getSize(),
                                Sort.by("registerDate").descending());
                Page<Car> cars = carRepository.findAll(pageable);

                List<CarShortInfoResponse> listCarInfoResponse = cars.getContent().stream().map(c -> {
                        CarShortInfoResponse carShortInfoResponse = modelMapper.map(c, CarShortInfoResponse.class);
                        carShortInfoResponse.setAddressInfo(c.getAddress().getDistrict().getName() + ", "
                                        + c.getAddress().getCity().getName());
                        carShortInfoResponse.setName(c.getModel().getName() + " " + c.getYearOfManufacture());
                        if (c.getCarImages().size() == 0) {
                                carShortInfoResponse.setCarImage(
                                                "https://assetscdn1.paytm.com/images/catalog/product/K/KI/KIDLIMOUSINE-DISKSE1140676506C2066/1565610984539_0..jpg");
                        } else {
                                carShortInfoResponse.setCarImage(c.getCarImages()
                                                .get(new Random().nextInt(c.getCarImages().size()))
                                                .getImage());
                        }

                        carShortInfoResponse.setTotalBook(c.getBooks().size());
                        List<Review> review = reviewRepository.findAllByBook_Car_Id(c.getId());

                        carShortInfoResponse.setTotalRating(Double.valueOf(
                                        new DecimalFormat("#0.0").format(review.stream()
                                                        .mapToInt(r -> r.getStarReview()).average()
                                                        .orElse(0.0))));

                        return carShortInfoResponse;
                }).collect(Collectors.toList());

                PagingResponse<CarShortInfoResponse> pagingResponse = PagingResponse
                                .<CarShortInfoResponse>builder()
                                .page(cars.getPageable().getPageNumber() + 1)
                                .size(cars.getSize())
                                .totalPage(cars.getTotalPages())
                                .totalItem(cars.getTotalElements())
                                .contends(listCarInfoResponse)
                                .build();
                return pagingResponse;
        }

        @Override
        @Transactional
        public List<CarShortInfoResponse> findAllCarsByCity(String code, PagingRequest pagingRequest) {

                Pageable pageable = PageRequest.of(pagingRequest.getPage() - 1, pagingRequest.getSize());
                // List<Car> listCar =
                // carRepository.findAllByStatusAndAddress_City_Code(CarStatus.ACTIVE, code,
                // pageable);
                Page<Car> listCar = carRepository.findCarByCityCodeSortByCountBookOfCar(CarStatus.ACTIVE.toString(),
                                code,
                                pageable);
                List<CarShortInfoResponse> listCarInfoResponse = listCar.getContent().stream().map(c -> {
                        CarShortInfoResponse carInfoResponse = modelMapper.map(c, CarShortInfoResponse.class);
                        carInfoResponse.setName(c.getModel().getName() + " " + c.getYearOfManufacture());
                        carInfoResponse.setAddressInfo(c.getAddress().getDistrict().getName() + ", "
                                        + c.getAddress().getCity().getName());
                        if (c.getCarImages().size() == 0) {
                                carInfoResponse.setCarImage(
                                                "https://assetscdn1.paytm.com/images/catalog/product/K/KI/KIDLIMOUSINE-DISKSE1140676506C2066/1565610984539_0..jpg");
                        } else {
                                carInfoResponse.setCarImage(c.getCarImages()
                                                .get(new Random().nextInt(c.getCarImages().size()))
                                                .getImage());
                        }

                        carInfoResponse.setTotalBook(c.getBooks().size());
                        List<Review> review = reviewRepository.findAllByBook_Car_Id(c.getId());

                        carInfoResponse.setTotalRating(Double.valueOf(
                                        new DecimalFormat("#0.0").format(review.stream()
                                                        .mapToInt(r -> r.getStarReview()).average()
                                                        .orElse(0.0))));

                        return carInfoResponse;
                }).collect(Collectors.toList());

                return listCarInfoResponse;
        }

        @Override
        @Transactional
        public void carBrowsing(CarBrowsingRequest carBrowsingRequest) {
                Car car = carRepository.findById(carBrowsingRequest.getId()).orElseThrow(
                                () -> new IllegalArgumentException("Car not found"));

                switch (carBrowsingRequest.getStatus()) {
                        case ACTIVE:
                                car.setStatus(CarStatus.ACTIVE);
                                break;
                        case DENIED:
                                car.setStatus(CarStatus.DENIED);
                                break;
                        case PAUSE:
                                car.setStatus(CarStatus.PAUSE);
                                break;
                        case PENDING_APPROVAL:
                                car.setStatus(CarStatus.PENDING_APPROVAL);
                                break;

                        default:
                                throw new IllegalStateException("Unknown status: " + carBrowsingRequest.getStatus());

                }

                carRepository.save(car);

        }

        @Override
        @Transactional
        public Object findAllCarByGuest(PagingRequest pagingRequest) {

                Pageable pageable = PageRequest.of(pagingRequest.getPage() - 1, pagingRequest.getSize());
                Page<Car> cars = carRepository.findCarByFamous(CarStatus.ACTIVE.toString(), pageable);
                // Page<Car> cars = carRepository.findAllByStatus(CarStatus.ACTIVE, pageable);

                List<CarShortInfoResponse> listCarInfoResponse = cars.getContent().stream().map(c -> {
                        CarShortInfoResponse carShortInfoResponse = modelMapper.map(c, CarShortInfoResponse.class);
                        carShortInfoResponse.setAddressInfo(c.getAddress().getDistrict().getName() + ", "
                                        + c.getAddress().getCity().getName());
                        carShortInfoResponse.setName(c.getModel().getName() + " " + c.getYearOfManufacture());
                        if (c.getCarImages().size() == 0) {
                                carShortInfoResponse.setCarImage(
                                                "https://assetscdn1.paytm.com/images/catalog/product/K/KI/KIDLIMOUSINE-DISKSE1140676506C2066/1565610984539_0..jpg");
                        } else {
                                carShortInfoResponse.setCarImage(c.getCarImages()
                                                .get(new Random().nextInt(c.getCarImages().size()))
                                                .getImage());
                        }
                        carShortInfoResponse.setTotalBook(c.getBooks().size());
                        List<Review> review = reviewRepository.findAllByBook_Car_Id(c.getId());

                        carShortInfoResponse.setTotalRating(Double.valueOf(
                                        new DecimalFormat("#0.0").format(review.stream()
                                                        .mapToInt(r -> r.getStarReview()).average()
                                                        .orElse(0.0))));

                        return carShortInfoResponse;
                }).collect(Collectors.toList());

                PagingResponse<CarShortInfoResponse> pagingResponse = PagingResponse
                                .<CarShortInfoResponse>builder()
                                .page(cars.getPageable().getPageNumber() + 1)
                                .size(cars.getSize())
                                .totalPage(cars.getTotalPages())
                                .totalItem(cars.getTotalElements())
                                .contends(listCarInfoResponse)
                                .build();
                return pagingResponse;
        }

        @Override
        @Transactional
        public Object filterCar(FilterCarSearchRequest filter, PagingRequest pagingRequest) {

                Pageable pageable = PageRequest.of(pagingRequest.getPage() - 1, pagingRequest.getSize());
                Page<Car> cars = carRepository.findAll(filterSearchCar(filter), pageable);

                List<CarShortInfoResponse> listCarInfoResponse = cars.getContent().stream().map(c -> {
                        CarShortInfoResponse carShortInfoResponse = modelMapper.map(c, CarShortInfoResponse.class);
                        carShortInfoResponse.setAddressInfo(c.getAddress().getDistrict().getName() + ", "
                                        + c.getAddress().getCity().getName());
                        carShortInfoResponse.setName(c.getModel().getName() + " " + c.getYearOfManufacture());

                        if (c.getCarImages().size() == 0) {
                                carShortInfoResponse.setCarImage(
                                                "https://assetscdn1.paytm.com/images/catalog/product/K/KI/KIDLIMOUSINE-DISKSE1140676506C2066/1565610984539_0..jpg");
                        } else {
                                carShortInfoResponse.setCarImage(c.getCarImages()
                                                .get(new Random().nextInt(c.getCarImages().size()))
                                                .getImage());
                        }

                        carShortInfoResponse.setTotalBook(c.getBooks().size());
                        List<Review> review = reviewRepository.findAllByBook_Car_Id(c.getId());

                        carShortInfoResponse.setTotalRating(Double.valueOf(
                                        new DecimalFormat("#0.0").format(review.stream()
                                                        .mapToInt(r -> r.getStarReview()).average()
                                                        .orElse(0.0))));
                        return carShortInfoResponse;
                }).collect(Collectors.toList());

                PagingResponse<CarShortInfoResponse> pagingResponse = PagingResponse
                                .<CarShortInfoResponse>builder()
                                .page(cars.getPageable().getPageNumber() + 1)
                                .size(cars.getSize())
                                .totalPage(cars.getTotalPages())
                                .totalItem(cars.getTotalElements())
                                .contends(listCarInfoResponse)
                                // .carBrands(carRepository
                                // .findAllBrandAndCountByFilterSearch(listIdCarBrands))
                                // .carModels(carRepository
                                // .findAllModelAndCountByFilterSearch(listIdCarModels,
                                // filter.getBrand_Id()))
                                .build();

                filter.setModel_Id_In(null);
                List<Car> carsListForModels = carRepository.findAll(filterSearchCar(filter));
                Long[] listIdCarModels = carsListForModels.stream().map(c -> c.getId()).toArray(Long[]::new);
                pagingResponse.setCarModels(carRepository
                                .findAllModelAndCountByFilterSearch(listIdCarModels,
                                                filter.getBrand_Id()));

                filter.setBrand_Id(null);
                List<Car> carsListForBrands = carRepository.findAll(filterSearchCar(filter));
                Long[] listIdCarBrands = carsListForBrands.stream().map(c -> c.getId()).toArray(Long[]::new);
                pagingResponse.setCarBrands(carRepository
                                .findAllBrandAndCountByFilterSearch(listIdCarBrands));

                return pagingResponse;
        }

        @Override
        @Transactional
        public void updateCar(CarUpdateInfoRequest carInfo) {

                Car car = carRepository.findById(carInfo.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Car not found"));
                Ward ward = wardRepository.findById(carInfo.getWard().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Vui lòng nhập địa chỉ"));
                modelMapper.map(carInfo, car);
                car.getCarImages().clear();
                car.getCarImages()
                                .addAll(modelMapper.map(carInfo.getCarImagesUpdate(), new TypeToken<List<CarImage>>() {
                                }.getType()));

                car.getFeatures().clear();
                car.getFeatures()
                                .addAll(modelMapper.map(carInfo.getFeaturesUpdate(), new TypeToken<Set<Feature>>() {
                                }.getType()));

                car.getAddress().setWard(ward);
                car.getAddress().setDistrict(ward.getDistrict());
                car.getAddress().setCity(ward.getDistrict().getCity());
                car.getAddress().setStreet(carInfo.getStreet());
                car.getCarImages().forEach(c -> c.setCar(car));
                carRepository.save(car);
        }

        @Override
        @Transactional
        public void deleteCar(Long id) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
                Car car = carRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Car not found"));
                switch (car.getStatus()) {
                        case PENDING_APPROVAL:
                                carRepository.delete(car);
                                break;
                        case DENIED:
                                carRepository.delete(car);
                                break;
                        case ACTIVE:
                                Boolean checkBooks = carRepository
                                                .existsByIdAndBooks_EndDateGreaterThanEqualAndBooks_Status(id,
                                                                cal.getTime(),
                                                                BookStatus.SUCCESS);
                                if (checkBooks) {
                                        throw new IllegalArgumentException("Xe đang có lịch thuê nên không thể xóa");
                                }
                                carRepository.delete(car);
                                break;
                        default:
                                throw new IllegalArgumentException("Vui lòng liên hệ admin để xóa xe");

                }
        }

        @Override
        @Transactional
        public Object getAllReviewByCarId(Long id, PagingRequest pagingRequest) {
                Pageable pageable = PageRequest.of(pagingRequest.getPage() - 1, pagingRequest.getSize(),
                                Sort.by("reviewDate").descending());
                Page<Review> reviews = reviewRepository.findAllByBook_Car_Id(id, pageable);

                List<ReviewByCarResponse> listReviewByCarResponse = reviews.getContent().stream().map(r -> {
                        ReviewByCarResponse reviewByCarResponse = modelMapper.map(r, ReviewByCarResponse.class);

                        long timeDiff = Math.abs(new Date().getTime() - r.getReviewDate().getTime());
                        long historyTime = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
                        if (historyTime > 30 && historyTime < 365) {
                                reviewByCarResponse.setHistoryTime(historyTime / 30 + " tháng trước");
                        } else if (historyTime <= 30) {
                                reviewByCarResponse.setHistoryTime(historyTime + " ngày trước");
                        } else if (historyTime >= 365) {
                                reviewByCarResponse.setHistoryTime(historyTime / 365 + " năm trước");
                        }

                        return reviewByCarResponse;
                }).collect(Collectors.toList());

                PagingResponse<ReviewByCarResponse> pagingResponse = PagingResponse
                                .<ReviewByCarResponse>builder()
                                .page(reviews.getPageable().getPageNumber() + 1)
                                .size(reviews.getSize())
                                .totalPage(reviews.getTotalPages())
                                .totalItem(reviews.getTotalElements())
                                .contends(listReviewByCarResponse)
                                .totalStarAverage(Double.valueOf(
                                                new DecimalFormat("#0.0").format(listReviewByCarResponse.stream()
                                                                .mapToInt(r -> r.getStarReview()).average()
                                                                .orElse(0.0))))
                                .build();

                return pagingResponse;
        }

}
