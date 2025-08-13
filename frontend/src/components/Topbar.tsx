import { Menu, X } from 'lucide-react';
import { login, logout } from "../utils/endpoints.ts";
import { Dropdown, DropdownItem } from "./Dropdown.tsx";
import { GoogleLogin } from '@react-oauth/google';

import type { User } from "../utils/types.ts";
import type { CredentialResponse } from '@react-oauth/google';

import MenuButton from "./buttons/MenuButton.tsx";
import clsx from "clsx";
import {useNavigate} from "react-router-dom";


type props = {
    sidebarToggled: boolean;
    setSidebarToggled: (toggled: boolean) => void;
    user: User | null;
    fetchUser: () => void;
    className?: string;
}

const Topbar = ({
                    sidebarToggled,
                    setSidebarToggled,
                    user,
                    fetchUser,
                    className}: props) => {

    const navigate = useNavigate();

    const handleLogin = (response: CredentialResponse) => {
        if (!response.credential) {
            console.error("No credential received from Google login.");
            return;
        }

        login(response.credential)
            .then(() => {
                fetchUser();
                navigate(0);
            })
            .catch((error) => {
                console.error("Login failed:", error);
            });
    }

    const handleLogout = () => {
        logout()
            .then(() => {
                fetchUser();
                navigate("/");
            })
            .catch((error) => {
                console.error("Logout failed:", error);
            });
    }

    return (
        <div className={clsx(className, "flex justify-between")}>
            <MenuButton onClick={() => setSidebarToggled(!sidebarToggled)}>
                {sidebarToggled ? <Menu size={24}/> :  <X size={24}/>}
            </MenuButton>

            { user ? (
                <div>
                    <img
                        className={"w-8 h-8 rounded-full inline-block"}
                        src={user.profilePictureUrl}
                        referrerPolicy="no-referrer"
                    />

                    <Dropdown label={user.name}>
                        <DropdownItem item="Logout"
                                      onClick={() => handleLogout()}
                                      className={"text-red-500 font-medium"} />
                    </Dropdown>
                </div>
            ) :
            (
                <GoogleLogin
                    shape={"pill"}
                    useOneTap={false}
                    onSuccess={(credentialResponse) => handleLogin(credentialResponse)} />
            )}
        </div>
    )
}

export default Topbar;