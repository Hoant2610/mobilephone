package com.datn.application.service.impl;

import com.datn.application.config.Contant;
import com.datn.application.entity.*;
import com.datn.application.exception.BadRequestException;
import com.datn.application.exception.InternalServerException;
import com.datn.application.exception.NotFoundException;
import com.datn.application.model.dto.OrderDetailDTO;
import com.datn.application.model.dto.OrderInfoDTO;
import com.datn.application.model.request.CreateOrderRequest;
import com.datn.application.model.request.UpdateDetailOrder;
import com.datn.application.model.request.UpdateStatusOrderRequest;
import com.datn.application.repository.OrderRepository;
import com.datn.application.repository.ProductRepository;
import com.datn.application.repository.ProductSizeRepository;
import com.datn.application.repository.StatisticRepository;
import com.datn.application.service.OrderService;
import com.datn.application.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Controller
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private StatisticRepository statisticRepository;

    @Override
    public Page<Order> adminGetListOrders(String id, String name, String phone, String status, String product, int page) {
        page--;
        if (page < 0) {
            page = 0;
        }
        int limit = 10;
        Pageable pageable = PageRequest.of(page, limit, Sort.by("created_at").descending());
        return orderRepository.adminGetListOrder(id, name, phone, status, product, pageable);
    }

    @Override
    public Order createOrder(CreateOrderRequest createOrderRequest, long userId) {

        //Kiểm tra sản phẩm có tồn tại
        Optional<Product> product = productRepository.findById(createOrderRequest.getProductId());
        if (product.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại!");
        }

        //Kiểm tra size có sẵn
        ProductSize productSize = productSizeRepository.checkProductAndSizeAvailable(createOrderRequest.getProductId(), createOrderRequest.getSize(),
            createOrderRequest.getColor());
        if (productSize == null) {
            throw new BadRequestException("Size sản phẩm tạm hết, Vui lòng chọn sản phẩm khác!");
        }

        //Kiểm tra giá sản phẩm
//        if (product.get().getSalePrice() * createOrderRequest.getQuantity() != createOrderRequest.getProductPrice()) {
//            throw new BadRequestException("Giá sản phẩm thay đổi, Vui lòng đặt hàng lại!");
//        }
        Order order = new Order();
        User user = new User();
        user.setId(userId);
        order.setCreatedBy(user);
        order.setBuyer(user);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        order.setReceiverAddress(createOrderRequest.getReceiverAddress());
        order.setReceiverName(createOrderRequest.getReceiverName());
        order.setReceiverPhone(createOrderRequest.getReceiverPhone());
        order.setNote(createOrderRequest.getNote());
        order.setSize(createOrderRequest.getSize());
        order.setPrice(createOrderRequest.getProductPrice());
        order.setTotalPrice(createOrderRequest.getTotalPrice());
        order.setStatus(Contant.ORDER_STATUS);
        order.setQuantity(createOrderRequest.getQuantity());
        order.setProduct(product.get());
        order.setColor(createOrderRequest.getColor());
        ProductSize ps = productSizeRepository.findByProductIdAndSizeAndColor(
            createOrderRequest.getProductId(), createOrderRequest.getSize(),
            createOrderRequest.getColor());
        ps.setQuantity(ps.getQuantity() - createOrderRequest.getQuantity());
        orderRepository.save(order);
        return order;

    }

    @Override
    public void updateDetailOrder(UpdateDetailOrder updateDetailOrder, long id, long userId) {
        //Kiểm trả có đơn hàng
        Optional<Order> rs = orderRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Đơn hàng không tồn tại");
        }

        Order order = rs.get();
        //Kiểm tra trạng thái đơn hàng
        if (order.getStatus() != Contant.ORDER_STATUS) {
            throw new BadRequestException("Chỉ cập nhật đơn hàng ở trạng thái chờ lấy hàng");
        }

        //Kiểm tra size sản phẩm
        Optional<Product> product = productRepository.findById(updateDetailOrder.getProductId());
        if (product.isEmpty()) {
            throw new BadRequestException("Sản phẩm không tồn tại");
        }
        //Kiểm tra giá
        if (product.get().getSalePrice() != updateDetailOrder.getProductPrice()) {
            throw new BadRequestException("Giá sản phẩm thay đổi vui lòng đặt hàng lại");
        }

        ProductSize productSize = productSizeRepository.checkProductAndSizeAvailable(updateDetailOrder.getProductId(), updateDetailOrder.getSize(),
            updateDetailOrder.getColor());
        if (productSize == null) {
            throw new BadRequestException("Size sản phẩm tạm hết, Vui lòng chọn sản phẩm khác");
        }

        //Kiểm tra khuyến mại
        if (updateDetailOrder.getCouponCode() != "") {
            Promotion promotion = promotionService.checkPromotion(updateDetailOrder.getCouponCode());
            if (promotion == null) {
                throw new NotFoundException("Mã khuyến mãi không tồn tại hoặc chưa được kích hoạt");
            }
            long promotionPrice = promotionService.calculatePromotionPrice(updateDetailOrder.getProductPrice(), promotion);
            if (promotionPrice != updateDetailOrder.getTotalPrice()) {
                throw new BadRequestException("Tổng giá trị đơn hàng thay đổi. Vui lòng kiểm tra và đặt lại đơn hàng");
            }
            Order.UsedPromotion usedPromotion = new Order.UsedPromotion(updateDetailOrder.getCouponCode(), promotion.getDiscountType(), promotion.getDiscountValue(), promotion.getMaximumDiscountValue());
            order.setPromotion(usedPromotion);
        }

        order.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        order.setProduct(product.get());
        order.setSize(updateDetailOrder.getSize());
        order.setPrice(updateDetailOrder.getProductPrice());
        order.setTotalPrice(updateDetailOrder.getTotalPrice());


        order.setStatus(Contant.ORDER_STATUS);
        User user = new User();
        user.setId(userId);
        order.setModifiedBy(user);
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            throw new InternalServerException("Lỗi khi cập nhật");
        }
    }


    @Override
    public Order findOrderById(long id) {
        Order order = orderRepository.findById(id).get();
        if (order == null) {
            throw new NotFoundException("Đơn hàng không tồn tại");
        }
        orderRepository.save(order);
        return order;
    }


    @Override
    public void updateStatusOrder(UpdateStatusOrderRequest updateStatusOrderRequest, long orderId, long userId) {
        Optional<Order> rs = orderRepository.findById(orderId);
        if (rs.isEmpty()) {
            throw new NotFoundException("Đơn hàng không tồn tại");
        }
        Order order = rs.get();
        //Kiểm tra trạng thái của đơn hàng
        boolean check = false;
        for (Integer status : Contant.LIST_ORDER_STATUS) {
            if (status == updateStatusOrderRequest.getStatus()) {
                check = true;
                break;
            }
        }
        if (!check) {
            throw new BadRequestException("Trạng thái đơn hàng không hợp lệ");
        }
        //Cập nhật trạng thái đơn hàng
        if (order.getStatus() == Contant.ORDER_STATUS || order.getStatus() == Contant.PAYMENT_STATUS) {
            //Đơn hàng ở trạng thái chờ lấy hàng
            if (updateStatusOrderRequest.getStatus() == Contant.ORDER_STATUS) {
                order.setReceiverPhone(updateStatusOrderRequest.getReceiverPhone());
                order.setReceiverName(updateStatusOrderRequest.getReceiverName());
                order.setReceiverAddress(updateStatusOrderRequest.getReceiverAddress());
                //Đơn hàng ở trạng thái đang vận chuyển
            } else if (updateStatusOrderRequest.getStatus() == Contant.DELIVERY_STATUS) {
                //Trừ đi một sản phẩm
//                productSizeRepository.minusOneProductBySize(order.getProduct().getId(), order.getSize());
                //Đơn hàng ở trạng thái đã giao hàng
            } else if (updateStatusOrderRequest.getStatus() == Contant.COMPLETED_STATUS) {
                //Trừ đi một sản phẩm và cộng một sản phẩm vào sản phẩm đã bán và cộng tiền
//                productSizeRepository.minusOneProductBySize(order.getProduct().getId(), order.getSize());
                productRepository.plusProductTotalSold(order.getProduct().getId(),order.getQuantity());
                productSizeRepository.plusProductTotalSold(order.getProduct().getId(),order.getSize(),order.getColor(),order.getQuantity());
                statistic(order.getTotalPrice(), order.getQuantity(), order);
            } else if (updateStatusOrderRequest.getStatus() != Contant.CANCELED_STATUS) {
                throw new BadRequestException("Không thế chuyển sang trạng thái này");
            }

        }
        //Đơn hàng ở trạng thái đang giao hàng
        else if (order.getStatus() == Contant.DELIVERY_STATUS) {
            //Đơn hàng ở trạng thái đã giao hàng
            if (updateStatusOrderRequest.getStatus() == Contant.COMPLETED_STATUS) {
                //Cộng một sản phẩm vào sản phẩm đã bán và cộng tiền
//                Cong o product
                productRepository.plusProductTotalSold(order.getProduct().getId(), order.getQuantity());
//                Cong o product_size
                productSizeRepository.plusProductTotalSold(order.getProduct().getId(),order.getSize(),order.getColor(),order.getQuantity());
                statistic(order.getTotalPrice(), order.getQuantity(), order);
                //Đơn hàng ở trạng thái đã hủy
            } else if (updateStatusOrderRequest.getStatus() == Contant.RETURNED_STATUS) {
                //Cộng lại một sản phẩm đã bị trừ
                productSizeRepository.plusProductBySize(order.getProduct().getId(), order.getSize(), order.getQuantity());
                //Đơn hàng ở trạng thái đã trả hàng
            } else if (updateStatusOrderRequest.getStatus() == Contant.CANCELED_STATUS) {
                //Cộng lại một sản phẩm đã bị trừ
                productSizeRepository.plusProductBySize(order.getProduct().getId(), order.getSize(),
                    order.getQuantity());
            } else if (updateStatusOrderRequest.getStatus() != Contant.DELIVERY_STATUS) {
                throw new BadRequestException("Không thế chuyển sang trạng thái này");
            }

        }
        //Đơn hàng ở trạng thái đã giao hàng
        else if (order.getStatus() == Contant.COMPLETED_STATUS) {
            //Đơn hàng đang ở trạng thái đã hủy
            if (updateStatusOrderRequest.getStatus() == Contant.RETURNED_STATUS) {
                //Cộng một sản phẩm đã bị trừ và trừ đi một sản phẩm đã bán và trừ số lượng
                productSizeRepository.plusProductBySize(order.getProduct().getId(), order.getSize(),order.getQuantity());
                productRepository.minusProductTotalSold(order.getProduct().getId(),
                    order.getQuantity());
                updateStatistic(order.getTotalPrice(), order.getQuantity(), order);
            } else if (updateStatusOrderRequest.getStatus() != Contant.COMPLETED_STATUS) {
                throw new BadRequestException("Không thế chuyển sang trạng thái này");
            }
        } else {
            if (order.getStatus() != updateStatusOrderRequest.getStatus()) {
                throw new BadRequestException("Không thế chuyển đơn hàng sang trạng thái này");
            }
        }

        User user = new User();
        user.setId(userId);
        order.setModifiedBy(user);
        order.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        order.setNote(updateStatusOrderRequest.getNote());
        order.setStatus(updateStatusOrderRequest.getStatus());
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            throw new InternalServerException("Lỗi khi cập nhật trạng thái");
        }
    }

    @Override
    public List<OrderInfoDTO> getListOrderOfPersonByStatus(int status, long userId) {
        List<OrderInfoDTO> list = orderRepository.getListOrderOfPersonByStatus(status, userId);

//        for (OrderInfoDTO dto : list) {
//            for (int i = 0; i < Contant.SIZE_VN.size(); i++) {
//                if (Contant.SIZE_VN.get(i) == dto.getSizeVn()) {
//                    dto.setSizeUs(Contant.SIZE_US[i]);
//                    dto.setSizeCm(Contant.SIZE_CM[i]);
//                }
//            }
//        }
        return list;
    }

    @Override
    public OrderDetailDTO userGetDetailById(long id, long userId) {
        OrderDetailDTO order = orderRepository.userGetDetailById(id, userId);
        if (order == null) {
            return null;
        }

        if (order.getStatus() == Contant.ORDER_STATUS) {
            order.setStatusText("Đơn hàng chưa thanh toán");
        } else if (order.getStatus() == Contant.DELIVERY_STATUS) {
            order.setStatusText("Đang giao hàng");
        } else if (order.getStatus() == Contant.COMPLETED_STATUS) {
            order.setStatusText("Đã giao hàng");
        } else if (order.getStatus() == Contant.CANCELED_STATUS) {
            order.setStatusText("Đơn hàng đã hủy");
        } else if (order.getStatus() == Contant.RETURNED_STATUS) {
            order.setStatusText("Đơn hàng đã trả lại");
        }else if (order.getStatus() == Contant.PAYMENT_STATUS){
            order.setStatusText("Đơn hàng đã thanh toán");
        }

//        for (int i = 0; i < Contant.SIZE_VN.size(); i++) {
//            if (Contant.SIZE_VN.get(i) == order.getSizeVn()) {
//                order.setSizeUs(Contant.SIZE_US[i]);
//                order.setSizeCm(Contant.SIZE_CM[i]);
//            }
//        }

        return order;
    }

    @Override
    public void userCancelOrder(long id, long userId) {
        Optional<Order> rs = orderRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Đơn hàng không tồn tại");
        }
        Order order = rs.get();
        if (order.getBuyer().getId() != userId) {
            throw new BadRequestException("Bạn không phải chủ nhân đơn hàng");
        }
        if (order.getStatus() != Contant.ORDER_STATUS) {
            throw new BadRequestException("Trạng thái đơn hàng không phù hợp để hủy. Vui lòng liên hệ với shop để được hỗ trợ");
        }

        order.setStatus(Contant.CANCELED_STATUS);
        orderRepository.save(order);
    }

    @Override
    public long getCountOrder() {
        return orderRepository.count();
    }

    public void statistic(long amount, int quantity, Order order) {
        Statistic statistic = statisticRepository.findByCreatedAT();
        if (statistic != null){
            statistic.setOrder(order);
            statistic.setSales(statistic.getSales() + amount);
            statistic.setQuantity(statistic.getQuantity() + quantity);
            statistic.setProfit(statistic.getProfit() + amount - order.getProduct().getPrice());
            statisticRepository.save(statistic);
        }else {
            Statistic statistic1 = new Statistic();
            statistic1.setOrder(order);
            statistic1.setSales(amount);
            statistic1.setQuantity(quantity);
            statistic1.setProfit(amount - (quantity * order.getProduct().getPrice()));
            statistic1.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            statisticRepository.save(statistic1);
        }
    }

    public void updateStatistic(long amount, int quantity, Order order) {
        Statistic statistic = statisticRepository.findByCreatedAT();
        if (statistic != null) {
            statistic.setOrder(order);
            statistic.setSales(statistic.getSales() - amount);
            statistic.setQuantity(statistic.getQuantity() - quantity);
            statistic.setProfit(statistic.getSales() - (statistic.getQuantity() * order.getProduct().getPrice()));
            statisticRepository.save(statistic);
        }
    }
}
