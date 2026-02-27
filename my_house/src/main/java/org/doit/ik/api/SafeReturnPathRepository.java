package org.doit.ik.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafeReturnPathRepository extends JpaRepository<SafeReturnPath, Long> {
}
