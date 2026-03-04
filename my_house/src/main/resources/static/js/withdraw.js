/**
 * 회원탈퇴 관련 스크립트 모듈
 */
const WithdrawModule = (() => {
    
    // 탈퇴 폼 제출 핸들러
    const handleWithdrawSubmit = (event) => {
        const checkbox = document.getElementById('confirmWithdraw');
        
        // 1. 체크박스 동의 여부 확인
        if (!checkbox || !checkbox.checked) {
            alert("탈퇴 안내 사항을 확인하고 체크박스에 동의해주세요.");
            event.preventDefault(); // 폼 제출 중단
            return false;
        }

        // 2. 최종 확인 팝업
        const isConfirmed = confirm("정말로 탈퇴하시겠습니까?\n탈퇴 시 모든 정보는 즉시 삭제되며 복구가 불가능합니다.");
        
        if (!isConfirmed) {
            event.preventDefault(); // 취소 시 폼 제출 중단
            return false;
        }
        
        return true;
    };

    // 초기화 함수: 이벤트 리스너 등록
    const init = () => {
        const withdrawForm = document.getElementById('withdrawForm');
        if (withdrawForm) {
            withdrawForm.addEventListener('submit', handleWithdrawSubmit);
        }
    };

    return {
        init: init
    };
})();

// 페이지 로드 시 모듈 초기화
document.addEventListener('DOMContentLoaded', WithdrawModule.init);