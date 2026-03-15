package com.rentloop.repository;

import com.rentloop.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, String> {
    List<Item> findByOwnerId(String ownerId);
    List<Item> findByStatusAndApprovedTrue(Item.Status status);
    List<Item> findByApprovedFalse();
    List<Item> findByCategoryAndStatusAndApprovedTrue(String category, Item.Status status);
    long countByApprovedFalse();
    long countByStatusAndApprovedTrue(Item.Status status);
}
