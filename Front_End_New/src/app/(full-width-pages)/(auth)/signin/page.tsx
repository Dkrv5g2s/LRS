"use client"; 
import SignInForm from "@/components/auth/SignInForm";
import { Metadata } from "next";
import { UserProvider } from "../../../../context/UserContext";


export default function SignIn() {
  return (
    <UserProvider>
      <SignInForm />
    </UserProvider>
  );
}
