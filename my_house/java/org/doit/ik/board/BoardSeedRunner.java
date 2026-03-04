package org.doit.ik.board;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardSeedRunner implements CommandLineRunner {

    private final MainCategoryRepository mainRepo;
    private final SubCategoryRepository subRepo;

    @Override
    public void run(String... args) {
        MainCategory mc = mainRepo.findByMcName("BOARD")
                .orElseGet(() -> {
                    MainCategory m = new MainCategory();
                    m.setMcName("BOARD");
                    return mainRepo.save(m);
                });

        createSubIfNotExists("공지사항", mc);
        createSubIfNotExists("자유게시판", mc);
        createSubIfNotExists("Q&A", mc);
    }

    private void createSubIfNotExists(String name, MainCategory mc) {
        subRepo.findByScName(name).orElseGet(() -> {
            SubCategory s = new SubCategory();
            s.setScName(name);
            s.setMainCategory(mc);
            return subRepo.save(s);
        });
    }
}