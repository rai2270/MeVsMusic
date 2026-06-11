package mvm.game;

// Callbacks from the game to the platform UI layer (implemented by FlyingActivity).
// Must stay free of Android/Rajawali/BASS types: this is the seam a port reimplements.
public interface GameEvents {
	void showLoader();
	void hideLoader();
	void removeCountDown();
	void onShipUpdate(int shipCount);
	void onStatusUpdate(String status, boolean countDown);
	void gameOverData(String title, long score);
	void gameOverStringTime();
	void gameOverTitleTime();
	void gameOverTop3Time();
	void gameOverTop31Time();
	void gameOverTop32Time();
	void gameOverTop33Time();
	void gameOverTime();
}
