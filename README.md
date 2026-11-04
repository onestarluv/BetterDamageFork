<div align="center">  

![0](https://github.com/user-attachments/assets/b72c6d7c-d614-4b88-975d-44beb3997570)

kokonut_damage에 오신 것을 환영합니다!

BetterDamage 기반 포크입니다. 플러그인 이름과 설정 폴더는 `kokonut_damage`입니다.
기존 설치는 서버를 종료한 뒤 `plugins/BetterDamage` 폴더를
`plugins/kokonut_damage`로 옮겨 설정과 리소스를 유지하세요.
명령어와 `betterdamage.*` 권한, 기존 DB 테이블명은 그대로 사용합니다.

[SpigotMC](https://www.spigotmc.org/resources/123850/) | [GitHub](https://github.com/toxicity188/BetterDamage) | [Wiki](https://github.com/toxicity188/BetterDamage/wiki)  

[![Discord](https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/rePyFESDbk)
[![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/toxicity188/BetterDamage?style=for-the-badge&logo=github)](https://github.com/toxicity188/BetterDamage/issues)

</div>

![](https://i.imgur.com/BCoo007.gif)  

### 마인크래프트 데미지 스킨 플러그인
- 리소스 팩 자동 생성
- PNG 파일로 데미지 스킨 자동 등록
- 권한으로 데미지 스킨 소유 여부 확인
- 보유 스킨 GUI와 클릭 장착
- 다양한 데미지 트리거 지원

### 연동 플러그인
- BetterModel, ModelEngine: 모델 높이 감지
- MythicLib: 데미지 이벤트 지원
- Kokonut-Core: 데이터베이스 및 플레이어 별명 조회

### 지원 플랫폼
- Spigot, Paper, Folia 및 해당 포크

### 지원 버전
- Minecraft 26.3 전용

### 빌드
./gradlew build

### 스킨 추가
`plugins/kokonut_damage/assets/skins/fire.png`처럼 PNG만 넣고
서버를 재시작하거나 Nexo 리소스 팩을 재생성하면 이미지·효과·스킨·GUI가 자동 등록됩니다.
파일명 `fire`가 스킨 ID이며, 권한은 `betterdamage.skin.fire`입니다.
파일명은 영문 소문자·숫자·밑줄·하이픈만 사용하세요.
PNG는 `0123456789` 순서의 동일한 크기 숫자 10개를 가로로 배치해야 합니다.
가로 길이는 10으로 나누어 떨어져야 합니다. 리소스 팩 갱신도 필요합니다.

효과의 이동·크기·표시 시간은 `SkinCatalog.kt`에서 공통 정의합니다.
공통 효과는 Y축 `0.7 + 0.5 * (t / 20)`으로 이동하며, 일반·치명타 모두 크기 `1`을 유지합니다.
X·Z 위치는 `0`, X·Y 회전은 `60(r - 0.5)`, Z 회전은 `0`입니다.
컬러 오버라이드는 제거하여 일반·치명타 모두 PNG 원본 색상을 유지합니다.
MythicLib 치명타는 주변 플레이어에게도 보여줍니다.
기존 `images/`, `effects/`, `skins/`의 YAML은 더 이상 읽지 않습니다.
기존 이미지는 원하는 스킨 ID로 이름을 바꿔 `assets/skins/`에 옮기세요.
같은 ID를 유지하면 기존 장착 데이터와 권한을 그대로 사용할 수 있습니다.

### 내장 스킨
아래 스킨은 기존 서버에도 시작·Nexo 리소스 팩 재생성 시 자동으로 추가됩니다.
같은 파일명이 이미 존재하면 덮어쓰지 않습니다.
이전 버전에서 임의로 붙인 ID의 내장 파일은 자동 삭제합니다.
ZIP 원본의 파일명을 그대로 사용하며, 기존 `default_skin` 기본 설정은 `default`로 연결합니다.
GUI·장착 안내·스킨 자동완성은 한국어 이름으로 표시합니다.
예: `/데미지스킨 장착 플레이어 하트` (영문 ID도 입력 가능)
저장 데이터와 권한에는 영문 ID를 사용합니다.

장착 GUI 아이콘은 PNG 파일명 기준 `<영문 ID>_coupon` 아이템에서 가져옵니다.
예: `heart.png` → `heart_coupon` (GUI 표시 이름은 하트).
재질과 `minecraft:item_model`만 새 아이템에 복사하고,
쿠폰의 이름·설명·인챈트·PDC 등은 복사하지 않습니다.
아이콘의 이름과 장착 안내는 GUI에서 설정합니다.
Nexo가 없거나 해당 쿠폰 ID가 없으면 기존 종이/책 아이콘을 사용합니다.

### 스킨 쿠폰 사용
Nexo와 LuckPerms가 활성화되어 있으면 `<영문 스킨 ID>_coupon` 아이템을
오른손 또는 왼손에 들고 우클릭하면 쿠폰 사용 확인 다이얼로그가 열립니다.
`확인`을 눌러야 권한 저장과 쿠폰 1개 소모가 진행됩니다.
`취소` 또는 Esc로 닫으면 권한·아이템을 변경하지 않습니다.
확인 시에도 현재 권한과 원래 슬롯의 쿠폰을 다시 검사합니다.
예: `heart_coupon` → `betterdamage.skin.heart` 영구 권한.
등록된 스킨과 정확히 일치하는 Nexo ID만 처리하며, 이름이나 모델만 같은 아이템은 처리하지 않습니다.
이미 해당 권한이 있으면 메시지·소모 없이 무시합니다. 그룹·와일드카드·OP 권한도 포함합니다.
LuckPerms API로 권한을 비동기 저장한 뒤 원래 슬롯의 쿠폰을 확인하고 정확히 1개 소모합니다.
저장 실패, 쿠폰 이동, 접속 종료 시에는 권한 변경을 되돌리고 쿠폰을 소모하지 않습니다.
연속 클릭은 처리 중 중복 차단합니다. 획득한 스킨은 장착 GUI에서 선택하세요.

| 한국어 이름 | 파일명 | 소유 권한 |
| --- | --- | --- |
| 브리즈 | `breeze.png` | `betterdamage.skin.breeze` |
| 기본 | `default.png` | `betterdamage.skin.default` |
| 할로윈 | `halloween.png` | `betterdamage.skin.halloween` |
| 하트 | `heart.png` | `betterdamage.skin.heart` |
| 한글 | `korean.png` | `betterdamage.skin.korean` |
| 호박 | `pumpkin.png` | `betterdamage.skin.pumpkin` |
| 스컬크 | `sculk.png` | `betterdamage.skin.sculk` |
| 영혼 | `soul.png` | `betterdamage.skin.soul` |
| 호랑이 | `tiger.png` | `betterdamage.skin.tiger` |

### 권한과 장착
스킨 소유 여부는 `betterdamage.skin.<스킨 ID>` 권한으로 판단합니다.
예를 들어 `default`는 `betterdamage.skin.default` 권한을 사용합니다.
LuckPerms 등 Bukkit 권한을 제공하는 플러그인에서 권한을 관리하세요.
지급·회수 명령어와 API(`add`, `remove`)는 제거했습니다.
기존 소유 목록은 사용하지 않으며 권한으로 자동 이전하지 않습니다.

`/데미지스킨`으로 보유한 스킨 목록을 열고 클릭해 장착합니다.
한 페이지에 45개를 표시하며 이전·다음 페이지와 현재 장착 표시를 지원합니다.
Core 칭호 UI와 동일하게 하단에 회색 유리판, 이전·다음 버튼, 페이지·보유 수,
장착 초기화 버튼을 표시합니다. 왼쪽 클릭 시 Core와 동일한 클릭음을 재생하며,
초기화하면 장착을 해제하고 공통 기본 스킨/효과를 적용합니다.
아이템 글꼴·기울임은 Core의 `ItemText` 규칙을 사용합니다.
개인 안내·오류는 Core 시스템 메시지 형식과 클라이언트 채팅 경로로 전달합니다.
클릭할 때와 데미지 효과를 사용할 때 권한을 다시 확인하므로,
권한 회수 후에는 설정의 기본 스킨/효과가 적용됩니다.
기본 스킨/효과는 미장착 플레이어에게도 적용되는 공통 기본값입니다.

### 명령어
`/bd`, `/betterdamage`도 `/데미지스킨`과 동일하게 사용할 수 있습니다.

| 명령어 | 설명 | 권한 |
| --- | --- | --- |
| `/데미지스킨` | 자신의 보유 스킨 장착 GUI | `betterdamage.gui` |
| `/데미지스킨 장착 <플레이어> <스킨>` | 대상에게 권한이 있는 스킨 장착 | `betterdamage.select` |

GUI 권한은 기본 허용이며 장착 명령은 기본적으로 OP가 사용합니다.
플레이어 인자는 접속 중인 계정명 또는 Kokonut-Core에 등록된 별명을 받습니다.
탭 자동완성에도 계정명과 별명을 제공하며 DB 조회는 비동기로 처리합니다.
미접속 플레이어는 현재 권한을 확인할 수 없어 장착 명령 대상에서 제외합니다.
도움말·목록·새로고침·시험·정보 하위 명령은 제거했습니다.
원본 플러그인의 업데이트 조회·사용자 정보 조회·bStats 통계 전송도 포함하지 않습니다.

### 데이터베이스
장착한 스킨만 저장하고 소유 목록은 저장하지 않습니다.
에고버스 전용으로 Kokonut-Core만 지원합니다. Kokonut-Core는 필수 의존성이며,
없으면 플러그인이 활성화되지 않습니다. `database.yml`은 생성하거나 읽지 않습니다.
기존 `database.yml`이 남아 있어도 무시하므로 삭제해도 됩니다.
Core의 `CoreDatabase` 서비스와 연결 설정을 재사용하며 별도 풀이나 JDBC 드라이버를 포함하지 않습니다.
테이블명은 Core 접두사 뒤에 `betterdamage_player_selected_skin`을 붙입니다.
작업 후 JDBC 연결을 반환하며 Core 서비스 자체를 종료하지 않습니다.

플러그인 시작 시 한 번 연결하고, 리소스 새로고침 때는 다시 연결하지 않습니다.
장착 정보가 변경된 플레이어만 저장하여 불필요한 DB 쓰기를 줄입니다.
기존 YAML/독립 MySQL 데이터는 자동 이전하지 않습니다.
기존 `player_skin` 테이블은 삭제하지 않지만 읽거나 쓰지 않습니다.
장착 정보는 기존 저장 형식과 호환되며, 해당 스킨 권한이 있어야 효과가 적용됩니다.

### API
[![](https://jitpack.io/v/toxicity188/BetterDamage.svg)](https://jitpack.io/#toxicity188/BetterDamage)
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies { 
    implementation("com.github.toxicity188:BetterDamage:Tag")
}
```
